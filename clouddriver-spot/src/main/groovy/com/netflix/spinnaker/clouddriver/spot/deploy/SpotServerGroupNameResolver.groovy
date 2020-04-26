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

package com.netflix.spinnaker.clouddriver.spot.deploy

import com.netflix.frigga.NameConstants
import com.netflix.frigga.NameValidation
import com.netflix.frigga.Names
import com.netflix.spinnaker.clouddriver.helpers.AbstractServerGroupNameResolver
import com.netflix.spinnaker.clouddriver.model.Cluster
import com.netflix.spinnaker.clouddriver.model.ClusterProvider
import com.spotinst.sdkjava.model.Elastigroup
import com.spotinst.sdkjava.model.ElastigroupGetAllRequest
import com.spotinst.sdkjava.model.SpotinstElastigroupClient
import groovy.transform.CompileStatic

@CompileStatic
class SpotServerGroupNameResolver extends AbstractServerGroupNameResolver {

  private static final String SPOT_PHASE = "SPOT_DEPLOY"
  private static final int DEFAULT_NEXT_SERVER_GROUP_ATTEMPTS = 5


  private final String accountName
  private final String region
  private final SpotinstElastigroupClient elastigroupClient
  private final Collection<ClusterProvider> clusterProviders
  private final int maxNextServerGroupAttempts

  SpotServerGroupNameResolver(String accountName,
                              String region,
                              SpotinstElastigroupClient elastigroupClient,
                              Collection<ClusterProvider> clusterProviders,
                              int maxNextServerGroupAttempts = DEFAULT_NEXT_SERVER_GROUP_ATTEMPTS) {
    this.accountName = accountName
    this.region = region
    this.elastigroupClient = elastigroupClient
    this.clusterProviders = clusterProviders
    this.maxNextServerGroupAttempts = maxNextServerGroupAttempts
  }

  @Override
  String getPhase() {
    return SPOT_PHASE
  }

  @Override
  String getRegion() {
    return region
  }

  @Override
  List<AbstractServerGroupNameResolver.TakenSlot> getTakenSlots(String clusterName) {
    def cluster = spotCluster(clusterProviders, accountName, clusterName)
    if (!cluster) {
      return []
    }
    Set<String> serverGroupNamesInCluster = cluster.serverGroups.collect { it.name }.toSet()
    List<Elastigroup> elastigroupsWithTakenServerGroupNames = new LinkedList<>()

    for (String serverGroupName : serverGroupNamesInCluster) {
      List<Elastigroup> elastigroups = getElastigroups(serverGroupName)
      def elastigroupsInRegion = elastigroups.findAll { it.region == region }

      if (elastigroupsInRegion.size() > 0) {
        elastigroupsWithTakenServerGroupNames.add(elastigroupsInRegion.get(0))
      }
    }

    return elastigroupsWithTakenServerGroupNames.collect {
      new AbstractServerGroupNameResolver.TakenSlot(
        serverGroupName: it.name,
        sequence: Names.parseName(it.name).sequence,
        createdTime: it.createdAt
      )
    }
  }

  private List<Elastigroup> getElastigroups(String serverGroupName) {
    ElastigroupGetAllRequest getAllRequest = ElastigroupGetAllRequest.Builder.get().setName(serverGroupName).build()
    def elastigroups = elastigroupClient.getAllElastigroups(getAllRequest)
    elastigroups
  }


  @Override
  String resolveNextServerGroupName(String application, String stack, String details, Boolean ignoreSequence) {
    def clusterName = combineAppStackDetail(application, stack, details)

    if (!NameValidation.checkNameWithHyphen(clusterName)) {
      throw new IllegalArgumentException("Invalid cluster name: '${clusterName}'. Cluster names can only contain " +
        "characters in the following range: ${NameConstants.NAME_HYPHEN_CHARS}")
    }

    def originalNextServerGroupName = super.resolveNextServerGroupName(application, stack, details, ignoreSequence)
    def nextServerGroupName = originalNextServerGroupName

    if (nextServerGroupName) {
      def hasNextServerGroup = false
      def attempts = 0
      while (!hasNextServerGroup && attempts++ <= DEFAULT_NEXT_SERVER_GROUP_ATTEMPTS) {
        // this resolver uses cached data to determine the next server group name so we should verify it does not already
        // exist before blindly using it
        def elastigroups = getElastigroups(nextServerGroupName)
        if (elastigroups.isEmpty()) {
          hasNextServerGroup = true
          break
        }

        def nextSequence = generateNextSequence(nextServerGroupName)
        nextServerGroupName = generateServerGroupName(application, stack, details, nextSequence, false)
      }

      if (!hasNextServerGroup) {
        throw new IllegalArgumentException("All server group names for cluster ${clusterName} in ${region} are taken.")
      }
    }

    return nextServerGroupName
  }

  private static Cluster spotCluster(Collection<ClusterProvider> clusterProviders, String accountName, String clusterName) {
    Collection<Cluster> clusters = clusterProviders.collect {
      def application = Names.parseName(clusterName).app
      it.getCluster(application, accountName, clusterName)
    }
    clusters.removeAll([null])

    return clusters.find { it.type.equalsIgnoreCase("spot") }
  }
}
