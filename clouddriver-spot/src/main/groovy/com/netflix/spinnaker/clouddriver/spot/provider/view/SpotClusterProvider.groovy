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
package com.netflix.spinnaker.clouddriver.spot.provider.view

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.frigga.Names
import com.netflix.spinnaker.cats.cache.Cache
import com.netflix.spinnaker.cats.cache.CacheData
import com.netflix.spinnaker.cats.cache.CacheFilter
import com.netflix.spinnaker.cats.cache.RelationshipCacheFilter
import com.netflix.spinnaker.clouddriver.model.ClusterProvider
import com.netflix.spinnaker.clouddriver.model.ServerGroupProvider
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsProvider
import com.netflix.spinnaker.clouddriver.spot.SpotCloudProvider
import com.netflix.spinnaker.clouddriver.spot.cache.Keys
import com.netflix.spinnaker.clouddriver.spot.model.SpotCluster
import com.netflix.spinnaker.clouddriver.spot.model.SpotServerGroup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import static com.netflix.spinnaker.clouddriver.core.provider.agent.Namespace.*

@Component
class SpotClusterProvider implements ClusterProvider<SpotCluster>, ServerGroupProvider {

  final Logger log = LoggerFactory.getLogger(getClass())

  final String cloudProviderId = SpotCloudProvider.ID

  final ObjectMapper objectMapper
  private AccountCredentialsProvider accountCredentialsProvider
  private final Cache cacheView

  @Autowired
  SpotClusterProvider(ObjectMapper objectMapper,
                      AccountCredentialsProvider accountCredentialsProvider,
                      Cache cacheView) {
    this.objectMapper = objectMapper
    this.accountCredentialsProvider = accountCredentialsProvider
    this.cacheView = cacheView
  }

  @Override
  Map<String, Set<SpotCluster>> getClusters() {
    Collection<CacheData> clusterData = cacheView.getAll(CLUSTERS.ns)
    Collection<SpotCluster> serverGroups = translateClusters(clusterData, false)
    mapResponse(serverGroups)
  }

  @Override
  Map<String, Set<SpotCluster>> getClusterSummaries(String applicationName) {
    getClusters0(applicationName, false)
  }

  @Override
  Map<String, Set<SpotCluster>> getClusterDetails(String applicationName) {
    getClusters0(applicationName, true)
  }

  @Override
  Set<SpotCluster> getClusters(String application, String account) {
    def retVal = []
    def clusterDetails = getClusterDetails(application)

    if (clusterDetails != null) {
      retVal = clusterDetails[account]
    }

    return retVal
  }

  @Override
  SpotCluster getCluster(String application, String account, String name, boolean includeDetails) {
    getClusters(application, account).find { name == it.name }
  }

  @Override
  SpotCluster getCluster(String application, String account, String name) {
    return getCluster(application, account, name, true)
  }

  @Override
  SpotServerGroup getServerGroup(String account, String region, String name, boolean includeDetails) {
    def pattern = Keys.getServerGroupKey(name, account, region)
    def identifiers = cacheView.filterIdentifiers(Keys.Namespace.SERVER_GROUPS.ns, pattern)
    Set<SpotServerGroup> serverGroups = loadServerGroups(identifiers)
    if (serverGroups.isEmpty()) {
      return null
    }
    return serverGroups.iterator().next()
  }

  @Override
  SpotServerGroup getServerGroup(String account, String region, String name) {
    return getServerGroup(account, region, name, true)
  }

  @Override
  boolean supportsMinimalClusters() {
    return false
  }

  private Set<SpotServerGroup> loadServerGroups(Collection<String> identifiers) {
    def data = cacheView.getAll(SERVER_GROUPS.ns, identifiers, RelationshipCacheFilter.none())
    return data.collect { cacheItem ->
      def sg = objectMapper.convertValue(cacheItem.attributes, SpotServerGroup)

      return sg
    }
  }

  @Override
  Collection<String> getServerGroupIdentifiers(String account, String region) {
    account = Optional.ofNullable(account).orElse("*")

    return cacheView.filterIdentifiers(SERVER_GROUPS.ns, Keys.getServerGroupKey("*", account, "*"))
  }

  @Override
  String buildServerGroupIdentifier(String account, String region, String serverGroupName) {
    return Keys.getServerGroupKey(serverGroupName, account, region)
  }

  private Collection<SpotCluster> translateClusters(Collection<CacheData> clusterData, boolean includeDetails) {
    Map<String, SpotServerGroup> serverGroups

    if (includeDetails) {
      log.warn("includeDetails = true inside translateClusters but not supported yet...")
      Collection<CacheData> allServerGroups = resolveRelationshipDataForCollection(clusterData, SERVER_GROUPS.ns, RelationshipCacheFilter.include(INSTANCES.ns, LAUNCH_CONFIGS.ns))
      serverGroups = translateServerGroups(allServerGroups)
      // instance relationships were expanded so no need to consider partial instances
    } else {
      Collection<CacheData> allServerGroups = resolveRelationshipDataForCollection(clusterData, SERVER_GROUPS.ns, RelationshipCacheFilter.none())
      serverGroups = translateServerGroups(allServerGroups)
    }

    Collection<SpotCluster> clusters = clusterData.collect { CacheData clusterDataEntry ->
      Map<String, String> clusterKey = Keys.parse(clusterDataEntry.id)

      SpotCluster cluster = new SpotCluster()
      cluster.accountName = clusterKey.account
      cluster.name = clusterKey.cluster
      cluster.serverGroups = clusterDataEntry.relationships[SERVER_GROUPS.ns]?.findResults { serverGroups.get(it) }

      cluster
    }

    return clusters
  }

  private Collection<CacheData> resolveRelationshipDataForCollection(Collection<CacheData> sources, String relationship, CacheFilter cacheFilter = null) {
    Collection<String> relationships = sources?.findResults { it.relationships[relationship] ?: [] }?.flatten() ?: []
    relationships ? cacheView.getAll(relationship, relationships, cacheFilter) : []
  }

  private Map<String, SpotServerGroup> translateServerGroups(Collection<CacheData> serverGroupData) {
    Map<String, SpotServerGroup> serverGroups = serverGroupData.collectEntries { serverGroupEntry ->
      SpotServerGroup serverGroup = new SpotServerGroup(serverGroupEntry.attributes)

      [(serverGroupEntry.id): serverGroup]
    }
    serverGroups
  }

  private static Map<String, Set<SpotCluster>> mapResponse(Collection<SpotCluster> clusters) {
    clusters.groupBy { it.accountName }.collectEntries { k, v -> [k, new HashSet(v)] } as Map<String, Set<SpotCluster>>
  }

  private Collection<CacheData> resolveRelationshipData(CacheData source, String relationship) {
    resolveRelationshipData(source, relationship) { true }
  }

  private Collection<CacheData> resolveRelationshipData(CacheData source, String relationship, Closure<Boolean> relFilter, CacheFilter cacheFilter = null) {
    Collection<String> filteredRelationships = source.relationships[relationship]?.findAll(relFilter)
    filteredRelationships ? cacheView.getAll(relationship, filteredRelationships, cacheFilter) : []
  }


  private Map<String, Set<SpotCluster>> getClusters0(String applicationName, boolean includeDetails) {
    CacheData application = cacheView.get(APPLICATIONS.ns, Keys.getApplicationKey(applicationName))
    if (application == null) {
      return null
    }

    Collection<SpotCluster> clusters

    if (includeDetails && cacheView.supportsGetAllByApplication()) {
      clusters = allClustersByApplication(applicationName)
    } else {
      clusters = translateClusters(resolveRelationshipData(application, CLUSTERS.ns), includeDetails)
    }
    mapResponse(clusters)
  }


  private Collection<SpotCluster> allClustersByApplication(String application) {
    // TODO: only supports the equiv of includeDetails=true, consider adding support for the inverse

    List<String> toFetch = [CLUSTERS.ns, SERVER_GROUPS.ns]

    def cacheResults = cacheView.getAllByApplication(toFetch, application, [:])


    Map<String, SpotServerGroup> serverGroups = translateServerGroups(
      cacheResults[SERVER_GROUPS.ns]
    )

    Collection<SpotCluster> clusters = cacheResults[CLUSTERS.ns].collect { clusterData ->
      Map<String, String> clusterKey = Keys.parse(clusterData.id)

      SpotCluster cluster = new SpotCluster()
      cluster.accountName = clusterKey.account
      cluster.name = clusterKey.cluster
      cluster.serverGroups = clusterData.relationships[SERVER_GROUPS.ns]?.findResults { serverGroups.get(it) }

      cluster
    }

    return clusters
  }
}
