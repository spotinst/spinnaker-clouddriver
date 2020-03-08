/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.spot.deploy.ops

import com.netflix.spinnaker.clouddriver.data.task.Task
import com.netflix.spinnaker.clouddriver.data.task.TaskRepository
import com.netflix.spinnaker.clouddriver.model.ServerGroup
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperation
import com.netflix.spinnaker.clouddriver.spot.deploy.description.ResizeElastigroupDescription
import com.netflix.spinnaker.clouddriver.spot.security.SpotClientProvider
import com.spotinst.sdkjava.model.Elastigroup
import com.spotinst.sdkjava.model.ElastigroupCapacityConfiguration
import com.spotinst.sdkjava.model.ElastigroupGetRequest
import com.spotinst.sdkjava.model.ElastigroupUpdateRequest

class ResizeElastigroupAtomicOperation implements AtomicOperation<Void> {
  private static final String PHASE = "RESIZE"

  private static Task getTask() {
    TaskRepository.threadLocalTask.get()
  }

  final ResizeElastigroupDescription description

  ResizeElastigroupAtomicOperation(ResizeElastigroupDescription description) {
    this.description = description
  }

  @Override
  Void operate(List priorOutputs) {
    String descriptor = description.elastigroups.collect { it.toString() }
    task.updateStatus PHASE, "Initializing Resize ELASTIGROUP operation for $descriptor..."

    for (elastigroup in description.elastigroups) {
      resizeElastigroup(elastigroup.serverGroupName, elastigroup.capacity, elastigroup.elastigroupId)
    }
    task.updateStatus PHASE, "Finished Resize ELASTIGROUP operation for $descriptor."
    null
  }

  private void resizeElastigroup(String elastigroupName,
                                 ServerGroup.Capacity capacity,
                                 String elastigroupId) {
    task.updateStatus PHASE, "Beginning resize of ${elastigroupName} to ${capacity}."

    if (capacity.min == null && capacity.max == null && capacity.desired == null) {
      task.updateStatus PHASE, "Skipping resize of ${elastigroupName}, at least one field in ${capacity} needs to be non-null"
      return
    }

    def elastigroupClient = new SpotClientProvider().getElastigroupClient(description.credentials.accountId)
    def getRequestBuilder = ElastigroupGetRequest.Builder.get()
    def getElastigroupRequest = getRequestBuilder.setElastigroupId(elastigroupId).build()
    def elastigroupToResize = elastigroupClient.getElastigroup(getElastigroupRequest)

    if (elastigroupToResize == null) {
      task.updateStatus PHASE, "Skipping resize of ${elastigroupName} with id ${elastigroupId}, server group does not exist"
      return
    }

    def capacityBuilder = ElastigroupCapacityConfiguration.Builder.get()
    ElastigroupCapacityConfiguration newCapacityConfig = capacityBuilder.setMaximum(capacity.max).setMinimum(capacity.min).setTarget(capacity.desired).build()
    def elastigroupBuilder = Elastigroup.Builder.get()
    def elastigroupWithUpdatedCapacity = elastigroupBuilder.setCapacity(newCapacityConfig).build()
    def updateElastigroupBuilder = ElastigroupUpdateRequest.Builder.get()
    def updateElastigroupRequest = updateElastigroupBuilder.setElastigroup(elastigroupWithUpdatedCapacity).build()
    def updatedSuccessfully = elastigroupClient.updateElastigroup(updateElastigroupRequest, elastigroupId)

    if (!updatedSuccessfully) {
      task.updateStatus PHASE, "Elasigroup: ${elastigroupName} was not resized"
      return
    }

    task.updateStatus PHASE, "Completed resize of ${elastigroupName}."
  }
}
