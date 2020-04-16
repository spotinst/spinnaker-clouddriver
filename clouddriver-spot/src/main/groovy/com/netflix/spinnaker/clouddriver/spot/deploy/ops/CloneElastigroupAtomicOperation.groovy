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
import com.netflix.spinnaker.clouddriver.spot.deploy.description.BasicCloneElastigroupDescription
import com.netflix.spinnaker.clouddriver.spot.provider.view.SpotClusterProvider
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
    def elastigroupIdToClone = extendedSource.elastigroup.id


    task.updateStatus BASE_PHASE, "Looking up last ELASTIGROUP: id = ${elastigroupIdToClone}, name = ${elastigroupNameToClone}"
//    def latestServerGroupName = sourceRegionScopedProvider.SPOTServerGroupNameResolver.resolveLatestServerGroupName(cluster)
//
//    if (latestServerGroupName) {
//      ancestorElastigroup = sourceRegionScopedProvider.elastigroupService.getElastigroup(latestServerGroupName)
//    }


//    def thisResult = basicSpotDeployHandler.handle(newDescription, priorOutputs)

//    result.serverGroupNames.addAll(thisResult.serverGroupNames)
//    result.deployedNames.addAll(thisResult.deployedNames)
//    result.deployments.addAll(thisResult.deployments)
//    result.createdArtifacts.addAll(thisResult.createdArtifacts)
//    result.messages.addAll(thisResult.messages)
//    thisResult.serverGroupNameByRegion.entrySet().each { result.serverGroupNameByRegion[it.key] = it.value }
//    thisResult.deployedNamesByLocation.entrySet().each { result.deployedNamesByLocation[it.key] = it.value }

    task.updateStatus BASE_PHASE, "Deployment complete for ELASTIGROUP $elastigroupIdToClone. New ELASTIGROUPs = ${result.serverGroupNames}"
    throw new IllegalStateException("'Clone Elastigroup' operation is not supported yet");
  }
}
