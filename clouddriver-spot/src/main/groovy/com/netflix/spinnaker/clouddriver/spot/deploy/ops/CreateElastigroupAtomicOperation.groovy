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
import com.netflix.spinnaker.clouddriver.spot.converters.sdk.ElastigroupCreationRequestConverter
import com.netflix.spinnaker.clouddriver.spot.deploy.description.CreateElastigroupDescription
import com.spotinst.sdkjava.model.Elastigroup
import com.spotinst.sdkjava.model.ElastigroupComputeConfiguration

class CreateElastigroupAtomicOperation implements AtomicOperation<DeploymentResult> {
  private static final String PHASE = "CREATE"

  private static Task getTask() {
    TaskRepository.threadLocalTask.get()
  }

  final CreateElastigroupDescription description

  CreateElastigroupAtomicOperation(CreateElastigroupDescription description) {
    this.description = description
  }

  @Override
  DeploymentResult operate(List priorOutputs) {
    task.updateStatus PHASE, "Initializing Create ELASTIGROUP operation for ${description.elastigroup.name}..."

    def elastigroup = createElastigroup(description.elastigroup)

    task.updateStatus PHASE, "Finished Create ELASTIGROUP operation for ${description.elastigroup.name}."

    DeploymentResult result = new DeploymentResult()

    if (elastigroup.region == null) {
      ElastigroupComputeConfiguration computeConfiguration = elastigroup.compute as ElastigroupComputeConfiguration
      String az = computeConfiguration.availabilityZones.get(0)["azName"]
      elastigroup.region = az.substring(0, az.length() - 1)
    }
    result.serverGroupNames.add(elastigroup.region + ":" + elastigroup.name)
    result.serverGroupNameByRegion.put(elastigroup.region, elastigroup.name)

    return result
  }

  private Elastigroup createElastigroup(Map<String, Object> elastigroupToCreate) {
    task.updateStatus PHASE, "Preparing Create ELASTIGROUP request..."

    def sdkCreateGroupRequest = ElastigroupCreationRequestConverter.toCreationRequest(elastigroupToCreate)

    task.updateStatus PHASE, "Sending Create ELASTIGROUP request..."

    def createdGroup = description.credentials.elastigroupClient.createElastigroup(sdkCreateGroupRequest)

    if (createdGroup != null) {
      task.updateStatus PHASE, "Successfully created ELASTIGROUP ${createdGroup.name}."
    }
    else {
      task.updateStatus PHASE, "Failed to Create ELASTIGROUP ${description.elastigroup.name}"
    }

    createdGroup
  }
}
