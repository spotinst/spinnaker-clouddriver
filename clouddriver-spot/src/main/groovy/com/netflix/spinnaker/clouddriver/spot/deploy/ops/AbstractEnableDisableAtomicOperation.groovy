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
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractEnableDisableAtomicOperation implements AtomicOperation<Void> {

  abstract boolean isDisable()

  abstract String getPhaseName()

  EnableDisableSpotServerGroupDescription description


  @Autowired
  ObjectMapper objectMapper

  AbstractEnableDisableAtomicOperation(EnableDisableSpotServerGroupDescription description) {
    this.description = description
  }

  @Override
  Void operate(List priorOutputs) {
    String verb = disable ? 'disable' : 'enable'
    String presentParticipling = disable ? 'Disabling' : 'Enabling'

    task.updateStatus phaseName, "Initializing $verb server group operation for $description.serverGroupName in " +
      "$description.region..."

    if (disable) {
      task.updateStatus phaseName, "$presentParticipling server group from Http(s) load balancers..."
      //todo yossi - implement
    } else {
      task.updateStatus phaseName, "Registering server group with Http(s) load balancers..."
      //todo yossi - implement
    }

    task.updateStatus phaseName, "$presentParticipling server group $description.serverGroupName in $description.region..."
    task.updateStatus phaseName, "Done ${presentParticipling.toLowerCase()} server group $description.serverGroupName in $description.region."
    return null
  }

  Task getTask() {
    TaskRepository.threadLocalTask.get()
  }
}
