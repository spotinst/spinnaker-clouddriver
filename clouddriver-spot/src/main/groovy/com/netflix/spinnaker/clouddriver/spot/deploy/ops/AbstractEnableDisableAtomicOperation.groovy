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
import com.spotinst.sdkjava.exception.SpotinstHttpException
import com.spotinst.sdkjava.model.*
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

    String elastigroupId = description.elastigroupId

    def serverGroup = spotClusterProvider.getServerGroup(description.account, description.region, description.serverGroupName)
    def loadBalancersConfig
    def scalingPolicyConfig

    def scalingConfigurationBuilder = ElastigroupScalingConfiguration.Builder.get()
    def computeConfigurationBuilder = ElastigroupComputeConfiguration.Builder.get()
    def launchSpecConfigurationBuilder = ElastigroupLaunchSpecification.Builder.get()
    def loadBalancerConfigBuilder = LoadBalancersConfig.Builder.get()

    if (disable) {
      task.updateStatus phaseName, "$presentParticipling server group from load balancers and scaling policies..."
      def currentLbConfig = serverGroup.elastigroup?.compute?.launchSpecification?.loadBalancersConfig as LoadBalancersConfig

      if (currentLbConfig) {
        serverGroup.setPreviousLoadBalancersConfig(currentLbConfig)
      }

      def currentScalingConfig = serverGroup.elastigroup?.scaling as ElastigroupScalingConfiguration

      if (currentScalingConfig) {
        serverGroup.setPreviousScalingConfiguation(currentScalingConfig)
      }

      loadBalancersConfig = loadBalancerConfigBuilder.setLoadBalancers(null).build()
      scalingPolicyConfig = scalingConfigurationBuilder.setDown(null).setUp(null).build()

    } else {
      task.updateStatus phaseName, "Registering server group with previous load balancers and scaling policies..."
      loadBalancersConfig = serverGroup.getPreviousLoadBalancersConfig()
      scalingPolicyConfig = serverGroup.getPreviousScalingConfiguation()

    }

    ElastigroupLaunchSpecification launchSpecification = launchSpecConfigurationBuilder.setLoadBalancersConfig(loadBalancersConfig).build()
    ElastigroupComputeConfiguration newComputeConfiguration = computeConfigurationBuilder.setLaunchSpecification(launchSpecification).build()

    def elastigroupBuilder = Elastigroup.Builder.get()
    def updatedElastigroup = elastigroupBuilder.setScaling(scalingPolicyConfig).setCompute(newComputeConfiguration).build()
    def updateElastigroupBuilder = ElastigroupUpdateRequest.Builder.get()
    def updateElastigroupRequest = updateElastigroupBuilder.setElastigroup(updatedElastigroup).build()

    try {
      description.credentials.getElastigroupClient().updateElastigroup(updateElastigroupRequest, elastigroupId)
      task.updateStatus phaseName, "Successfully updated elastigroup..."
    }
    catch (SpotinstHttpException exception) {
      task.updateStatus phaseName, "Failed to update elastigroup, Error: " + exception.getMessage()
    }

    task.updateStatus phaseName, "Done ${presentParticipling.toLowerCase()} server group $description.serverGroupName in $description.region."
  }

  Task getTask() {
    TaskRepository.threadLocalTask.get()
  }
}
