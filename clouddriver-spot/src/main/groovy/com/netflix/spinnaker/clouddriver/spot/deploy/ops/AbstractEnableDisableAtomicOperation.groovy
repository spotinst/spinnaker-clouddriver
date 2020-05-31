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

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.clouddriver.data.task.Task
import com.netflix.spinnaker.clouddriver.data.task.TaskRepository
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperation
import com.netflix.spinnaker.clouddriver.spot.deploy.description.EnableDisableSpotServerGroupDescription
import com.netflix.spinnaker.clouddriver.spot.provider.view.SpotClusterProvider
import com.spotinst.sdkjava.enums.ProcessNameEnum
import com.spotinst.sdkjava.model.ElastigroupRemoveSuspensionsRequest
import com.spotinst.sdkjava.model.ElastigroupStandbyRequest
import com.spotinst.sdkjava.model.ElastigroupSuspendProcessesRequest
import com.spotinst.sdkjava.model.ProcessSuspension
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractEnableDisableAtomicOperation implements AtomicOperation<Void> {

  abstract boolean isDisable()

  abstract String getPhaseName()

  @Autowired
  private SpotClusterProvider spotClusterProvider

  EnableDisableSpotServerGroupDescription description


  @Autowired
  ObjectMapper objectMapper

  AbstractEnableDisableAtomicOperation(EnableDisableSpotServerGroupDescription description) {
    this.description = description
  }

  @Override
  Void operate(List priorOutputs) {
    String presentParticipling = disable ? 'Disabling' : 'Enabling'
    task.updateStatus phaseName, "$presentParticipling server group $description.serverGroupName in $description.region..."
    def serverGroup = spotClusterProvider.getServerGroup(description.account, description.region, description.serverGroupName)
    String elastigroupId = serverGroup.elastigroup.id
    ElastigroupStandbyRequest.Builder elastigroupStandbyRequestBuilder = ElastigroupStandbyRequest.Builder.get()
    ElastigroupStandbyRequest standbyRequest = elastigroupStandbyRequestBuilder.setElastigroupId(elastigroupId).build()

    if (disable) {
      description.credentials.getElastigroupClient().enterGroupStandby(standbyRequest)
      suspendAutoScaling(elastigroupId)
    } else {
      description.credentials.getElastigroupClient().exitGroupStandby(standbyRequest)
      removeAutoScalingSuspension(elastigroupId)
    }

    task.updateStatus phaseName, "Done ${presentParticipling.toLowerCase()} server group $description.serverGroupName in $description.region."
  }

  private void suspendAutoScaling(String elastigroupId) {
    task.updateStatus phaseName, "Suspending auto scaling activities"
    ElastigroupSuspendProcessesRequest.Builder requestBuilder = ElastigroupSuspendProcessesRequest.Builder.get()
    ProcessSuspension.Builder suspensionBuilder = ProcessSuspension.Builder.get()
    ProcessSuspension suspension = suspensionBuilder.setName(ProcessNameEnum.AUTO_SCALE).setTtlInMinutes(null).build()
    List<ProcessSuspension> suspensions = Collections.singletonList(suspension)
    ElastigroupSuspendProcessesRequest request =
      requestBuilder.setElastigroupId(elastigroupId).setSuspensions(suspensions).build()
    description.credentials.getElastigroupClient().suspendProcess(request)
  }

  private void removeAutoScalingSuspension(String elastigroupId) {
    task.updateStatus phaseName, "Removing suspensions of auto scaling activities"
    ElastigroupRemoveSuspensionsRequest.Builder requestBuilder = ElastigroupRemoveSuspensionsRequest.Builder.get()
    List<ProcessNameEnum> toRemove = Collections.singletonList(ProcessNameEnum.AUTO_SCALE)
    ElastigroupRemoveSuspensionsRequest request =
      requestBuilder.setElastigroupId(elastigroupId).setProcesses(toRemove).build()
    description.credentials.getElastigroupClient().removeSuspensions(request)
  }

  Task getTask() {
    TaskRepository.threadLocalTask.get()
  }
}
