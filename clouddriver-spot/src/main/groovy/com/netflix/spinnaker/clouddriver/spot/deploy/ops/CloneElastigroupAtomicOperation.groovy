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
import com.netflix.spinnaker.clouddriver.deploy.DeploymentResult
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperation
import com.netflix.spinnaker.clouddriver.spot.deploy.SpotServerGroupNameResolver
import com.netflix.spinnaker.clouddriver.spot.deploy.description.BasicCloneElastigroupDescription
import com.netflix.spinnaker.clouddriver.spot.provider.view.SpotClusterProvider
import com.spotinst.sdkjava.exception.SpotinstHttpException
import com.spotinst.sdkjava.model.Elastigroup
import com.spotinst.sdkjava.model.ElastigroupCloneRequest
import com.spotinst.sdkjava.model.ElastigroupComputeConfiguration
import com.spotinst.sdkjava.model.ElastigroupLaunchSpecification
import org.springframework.beans.factory.annotation.Autowired

class CloneElastigroupAtomicOperation implements AtomicOperation<DeploymentResult> {
  private static final String BASE_PHASE = "CLONE_ELASTIGROUP"

  private static Task getTask() {
    TaskRepository.threadLocalTask.get()
  }

  final BasicCloneElastigroupDescription description

  @Autowired
  private SpotClusterProvider spotClusterProvider

  CloneElastigroupAtomicOperation(BasicCloneElastigroupDescription description) {
    this.description = description
  }

  @Override
  DeploymentResult operate(List priorOutputs) {
    task.updateStatus BASE_PHASE, "Initializing Clone Last ELASTIGROUP Operation..."

    def newImageId = description.getImageId()
    def basicSource = this.description.source
    def extendedSource = spotClusterProvider.getServerGroup(basicSource.account, basicSource.region, basicSource.asgName)
    SpotServerGroupNameResolver nameResolver = new SpotServerGroupNameResolver(basicSource.account, basicSource.region, description.credentials.elastigroupClient, [spotClusterProvider])
    def newName = nameResolver.resolveNextServerGroupName(description.application, description.stack, description.freeFormDetails, false)
    String elastigroupIdToClone = extendedSource.elastigroup.id

    def clonedElastigroup = cloneElastigroup(elastigroupIdToClone, newName, newImageId)
    DeploymentResult result = new DeploymentResult()

    if (clonedElastigroup.region == null) {
      ElastigroupComputeConfiguration computeConfiguration = clonedElastigroup.compute as ElastigroupComputeConfiguration
      String az = computeConfiguration.availabilityZones.get(0)["azName"]
      clonedElastigroup.region = az.substring(0, az.length() - 1)
    }
    result.serverGroupNames.add(clonedElastigroup.region + ":" + clonedElastigroup.name)
    result.serverGroupNameByRegion.put(clonedElastigroup.region, clonedElastigroup.name)

    return result
  }

  Elastigroup cloneElastigroup(String elastigroupId, String newName, String newImageId) {
    task.updateStatus BASE_PHASE, "Start clone ${elastigroupId} with name ${newName}, image id ${newImageId}."


    def launchSpecificationBuilder = ElastigroupLaunchSpecification.Builder.get()
    ElastigroupLaunchSpecification launchSpecification = launchSpecificationBuilder.setImageId(newImageId).build()
    def computeBuilder = ElastigroupComputeConfiguration.Builder.get()
    ElastigroupComputeConfiguration computeConfiguration = computeBuilder.setLaunchSpecification(launchSpecification).build()

    def elastigroupBuilder = Elastigroup.Builder.get()
    def elastigroup = elastigroupBuilder.setCompute(computeConfiguration).setName(newName).build()
    def cloneElastigroupBuilder = ElastigroupCloneRequest.Builder.get()
    def cloneElastigroupRequest = cloneElastigroupBuilder.setElastigroup(elastigroup).build()

    try {
      Elastigroup clonedElastigroup = description.credentials.elastigroupClient.cloneElastigroup(cloneElastigroupRequest, elastigroupId)
      task.updateStatus BASE_PHASE, "Finished cloning. New elastigroup id: ${clonedElastigroup.id} "
      return clonedElastigroup
    }
    catch (SpotinstHttpException ex) {
      task.updateStatus BASE_PHASE, "Failed to clone elastigroup, Error: " + ex.getMessage()
    }
  }
}
