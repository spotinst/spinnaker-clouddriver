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
import com.spotinst.sdkjava.model.*
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
    String elastigroupId = description.elastigroupId

    if (disable) {
      task.updateStatus phaseName, "$presentParticipling server group from load balancers and scaling policies..."
      def scalingConfigurationBuilder = ElastigroupScalingConfiguration.Builder.get()
      def computeConfigurationBuilder = ElastigroupComputeConfiguration.Builder.get()
      def launchSpecConfigurationBuilder = ElastigroupLaunchSpecification.Builder.get()
      def loadBalancerConfigBuilder = LoadBalancersConfig.Builder.get()

      def loadBalancersConfig = loadBalancerConfigBuilder.setLoadBalancers(null).build()
      ElastigroupLaunchSpecification launchSpecification = launchSpecConfigurationBuilder.setLoadBalancersConfig(loadBalancersConfig).build()

      ElastigroupScalingConfiguration newScalingConfiguration = scalingConfigurationBuilder.setDown(null).setUp(null).build()
      ElastigroupComputeConfiguration newComputeConfiguration = computeConfigurationBuilder.setLaunchSpecification(launchSpecification).build()

      def elastigroupBuilder = Elastigroup.Builder.get()
      def updatedElastigroup = elastigroupBuilder.setScaling(newScalingConfiguration).setCompute(newComputeConfiguration).build()
      def updateElastigroupBuilder = ElastigroupUpdateRequest.Builder.get()
      def updateElastigroupRequest = updateElastigroupBuilder.setElastigroup(updatedElastigroup).build()

      def updatedSuccessfully = description.credentials.getElastigroupClient().updateElastigroup(updateElastigroupRequest, elastigroupId)

      if (updatedSuccessfully) {
        task.updateStatus phaseName, "Successfully disabled elastigroup..."

      }
    } else {
      //todo yossi should throw?
      task.updateStatus phaseName, "Registering server group with load balancers..."
    }

    task.updateStatus phaseName, "$presentParticipling server group $description.serverGroupName in $description.region..."
    task.updateStatus phaseName, "Done ${presentParticipling.toLowerCase()} server group $description.serverGroupName in $description.region."
    return null
  }

  Task getTask() {
    TaskRepository.threadLocalTask.get()
  }
}
