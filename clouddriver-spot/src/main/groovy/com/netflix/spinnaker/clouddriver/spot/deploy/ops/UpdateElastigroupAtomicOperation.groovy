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
import com.netflix.spinnaker.clouddriver.deploy.DeploymentResult
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperation
import com.netflix.spinnaker.clouddriver.orchestration.events.DeleteServerGroupEvent
import com.netflix.spinnaker.clouddriver.orchestration.events.OperationEvent
import com.netflix.spinnaker.clouddriver.spot.SpotCloudProvider
import com.netflix.spinnaker.clouddriver.spot.converters.sdk.ElastigroupUpdateRequestConverter
import com.netflix.spinnaker.clouddriver.spot.deploy.description.UpdateElastigroupDescription
import com.spotinst.sdkjava.model.Elastigroup
import com.spotinst.sdkjava.model.ElastigroupComputeConfiguration

class UpdateElastigroupAtomicOperation implements AtomicOperation<Void> {
  private static final String PHASE = "UPDATE"

  private static Task getTask() {
    TaskRepository.threadLocalTask.get()
  }

  final UpdateElastigroupDescription description

    UpdateElastigroupAtomicOperation(UpdateElastigroupDescription description) {
    this.description = description
  }

  @Override
  Void operate(List priorOutputs) {
    task.updateStatus PHASE, "Initializing Update ELASTIGROUP operation for ${description.elastigroup.name}..."

    def elastigroup = updateElastigroup(description.elastigroup, description.elastigroupId)

    task.updateStatus PHASE, "Finished Update ELASTIGROUP operation for ${description.elastigroup.name}."
    null
  }

  private Elastigroup updateElastigroup(Map<String, Object> elastigroupToUpdate, String elastigroupId) {
    task.updateStatus PHASE, "Preparing Update ELASTIGROUP request..."

    def sdkUpdateGroupRequest = ElastigroupUpdateRequestConverter.toUpdateRequest(elastigroupToUpdate)

    task.updateStatus PHASE, "Sending Update ELASTIGROUP request..."

    def updatedGroup = description.credentials.elastigroupClient.updateElastigroup(sdkUpdateGroupRequest, elastigroupId)

    if (updatedGroup != null) {
      task.updateStatus PHASE, "Successfully Updated ELASTIGROUP ${createdGroup.name}."
    }
    else {
      task.updateStatus PHASE, "Failed to Update ELASTIGROUP ${description.elastigroup.name}"
    }

    updatedGroup
  }
}
