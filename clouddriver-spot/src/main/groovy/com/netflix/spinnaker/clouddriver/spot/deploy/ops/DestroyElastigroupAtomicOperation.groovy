/*
 * Copyright 2020 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperation
import com.netflix.spinnaker.clouddriver.orchestration.events.DeleteServerGroupEvent
import com.netflix.spinnaker.clouddriver.orchestration.events.OperationEvent
import com.netflix.spinnaker.clouddriver.spot.SpotCloudProvider
import com.netflix.spinnaker.clouddriver.spot.deploy.description.DestroyElastigroupDescription
import com.spotinst.sdkjava.model.ElastigroupDeletionRequest
import com.spotinst.sdkjava.model.ElastigroupGetRequest

class DestroyElastigroupAtomicOperation implements AtomicOperation<Void> {
  private static final String BASE_PHASE = "DESTROY_ELASTIGROUP"

  private static Task getTask() {
    TaskRepository.threadLocalTask.get()
  }

  private final DestroyElastigroupDescription description
  private final Collection<DeleteServerGroupEvent> events = []

  DestroyElastigroupAtomicOperation(DestroyElastigroupDescription description) {
    this.description = description
  }

  @Override
  Void operate(List priorOutputs) {
    String descriptor = description.elastigroups.collect { it.toString() }
    task.updateStatus BASE_PHASE, "Initializing ELASTIGROUP Destroy operation for $descriptor..."

    String accountId = description.credentials.accountId

    for (elastigroup in description.elastigroups) {
      deleteElastigroup(elastigroup.elastigroupId)
      events << new DeleteServerGroupEvent(
        SpotCloudProvider.ID, accountId, elastigroup.region, elastigroup.serverGroupName
      )
    }

    task.updateStatus BASE_PHASE, "Finished Destroy ELASTIGROUP operation for $descriptor."
    null
  }

  @Override
  Collection<OperationEvent> getEvents() {
    return events
  }

  private void deleteElastigroup(String elastigroupId) {

    def getRequestBuilder = ElastigroupGetRequest.Builder.get()
    def getElastigroupRequest = getRequestBuilder.setElastigroupId(elastigroupId).build()
    def elastigroupToDestroy = description.credentials.elastigroupClient.getElastigroup(getElastigroupRequest)

    if (!elastigroupToDestroy) {
      task.updateStatus BASE_PHASE, "Skipping destruction of $description.elastigroupName with id $elastigroupId - server group does not exist"
      return // Okay, there is no auto scaling group. Let's be idempotent and not complain about that.
    }

    def destroyRequestBuilder = ElastigroupDeletionRequest.Builder.get()
    def destroyElastigroupRequest = destroyRequestBuilder.setElastigroupId(elastigroupId).build()
    Boolean deleted = description.credentials.elastigroupClient.deleteElastigroup(destroyElastigroupRequest)

    if (!deleted) {
      task.updateStatus BASE_PHASE, "Elasigroup: ${description.elastigroupName} was not destroyed"
      return
    }
  }

}
