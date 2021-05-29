/*
 * Copyright 2021 Netflix, Inc.
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
import com.netflix.spinnaker.clouddriver.data.task.TaskRepository;
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperation
import com.netflix.spinnaker.clouddriver.spot.deploy.description.TerminateInstancesDescription
import com.spotinst.sdkjava.model.ElastigroupDetachInstancesRequest

class TerminateInstancesAtomicOperation implements AtomicOperation<Void> {
  private static final String BASE_PHASE = "TERMINATE_INSTANCES"

  private static Task getTask() {
    TaskRepository.threadLocalTask.get()
  }

  private final TerminateInstancesDescription description;

  TerminateInstancesAtomicOperation(TerminateInstancesDescription description) {
    this.description = description
  }

  @Override
  public Void operate(List priorOutputs) {
    task.updateStatus BASE_PHASE, "Initializing termination of instances (${description.instanceIds.join(", ")})."

    def detachInstancesRequest = ElastigroupDetachInstancesRequest.Builder.get()
      .setInstancesToDetach(description.instanceIds)
      .setShouldTerminateInstances(true)
      .build()

    description.credentials.elastigroupClient.detachInstances(detachInstancesRequest, description.elastiGroup);

    task.updateStatus BASE_PHASE, "Done executing termination of instances (${description.instanceIds.join(", ")})."
    return null
  }
}
