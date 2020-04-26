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

package com.netflix.spinnaker.clouddriver.spot.provider.agent

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.netflix.frigga.Names
import com.netflix.spectator.api.Registry
import com.netflix.spinnaker.cats.agent.*
import com.netflix.spinnaker.cats.cache.CacheData
import com.netflix.spinnaker.cats.cache.DefaultCacheData
import com.netflix.spinnaker.cats.provider.ProviderCache
import com.netflix.spinnaker.clouddriver.cache.OnDemandAgent
import com.netflix.spinnaker.clouddriver.cache.OnDemandMetricsSupport
import com.netflix.spinnaker.clouddriver.spot.SpotCloudProvider
import com.netflix.spinnaker.clouddriver.spot.cache.Keys
import com.netflix.spinnaker.clouddriver.spot.provider.view.MutableCacheData
import com.netflix.spinnaker.clouddriver.spot.security.SpotAccountCredentials
import com.spotinst.sdkjava.model.Elastigroup
import com.spotinst.sdkjava.model.ElastigroupGetAllRequest
import com.spotinst.sdkjava.model.ElastigroupGetInstanceHealthinessRequest
import com.spotinst.sdkjava.model.ElastigroupInstanceHealthiness
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static com.netflix.spinnaker.cats.agent.AgentDataType.Authority.AUTHORITATIVE
import static com.netflix.spinnaker.clouddriver.core.provider.agent.Namespace.*

/**
 * A caching agent for Spot server groups.
 *
 * The groups are collected cloud-side by the SpotinstElastigroupClient. In this agent we just read
 * all server groups that we can see given our credentials.
 *
 * This may be a slow operation due to the large number of API calls that the service makes.
 *
 * Created by yossi.elman on 16/02/2020.
 */


class SpotServerGroupCachingAgent implements CachingAgent, OnDemandAgent, AccountAware {
  final Logger log = LoggerFactory.getLogger(getClass())

  class NotImplementedException extends RuntimeException {

  }

  private static final TypeReference<Map<String, Object>> ATTRIBUTES = new TypeReference<Map<String, Object>>() {}

  static final Set<AgentDataType> types = Collections.unmodifiableSet([
    AUTHORITATIVE.forType(SERVER_GROUPS.ns),
    AUTHORITATIVE.forType(CLUSTERS.ns),
    AUTHORITATIVE.forType(APPLICATIONS.ns),
  ] as Set)

  final String accountName
  final SpotAccountCredentials account
  final String region
  final ObjectMapper objectMapper
  final Registry registry

  final OnDemandMetricsSupport metricsSupport

  SpotServerGroupCachingAgent(SpotAccountCredentials account,
                              ObjectMapper objectMapper,
                              Registry registry) {
    this.account = account
    this.accountName = account.getName()
    this.objectMapper = objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    this.registry = registry
    this.metricsSupport = new OnDemandMetricsSupport(registry, this, "${SpotCloudProvider.ID}:${OnDemandAgent.OnDemandType.ServerGroup}")
  }

  @Override
  String getProviderName() {
    SpotCloudProvider.ID
  }

  @Override
  String getAgentType() {
    "${this.getAccountName()}/${SpotServerGroupCachingAgent.simpleName}"
  }

  @Override
  String getOnDemandAgentType() {
    "${getAgentType()}-OnDemand"
  }

  @Override
  Collection<AgentDataType> getProvidedDataTypes() {
    types
  }

  @Override
  CacheResult loadData(ProviderCache providerCache) {
    log.debug("Describing items in ${agentType}")

    Long start = System.currentTimeMillis()

    def elastigroups = loadElastigroups()

    def evictableOnDemandCacheDatas = []
    def usableOnDemandCacheDatas = []

    def serverGroupKeys = elastigroups.collect {
      Keys.getServerGroupKey(it.name, accountName, it.region)
    } as Set<String>
    def pendingOnDemandRequestKeys = providerCache
      .filterIdentifiers(ON_DEMAND.ns, Keys.getServerGroupKey("*", accountName, "*"))
      .findAll { serverGroupKeys.contains(it) }

    def pendingOnDemandRequestsForServerGroups = providerCache.getAll(ON_DEMAND.ns, pendingOnDemandRequestKeys)
    pendingOnDemandRequestsForServerGroups.each {
      if (it.attributes.cacheTime < start && it.attributes.processedCount > 0) {
        evictableOnDemandCacheDatas << it
      } else {
        usableOnDemandCacheDatas << it
      }
    }
    CacheResult result = buildCacheResults(elastigroups)
    def cacheResults = result.cacheResults
    log.debug("Caching ${cacheResults[APPLICATIONS.ns]?.size()} applications in ${agentType}")
    log.debug("Caching ${cacheResults[CLUSTERS.ns]?.size()} clusters in ${agentType}")
    log.debug("Caching ${cacheResults[SERVER_GROUPS.ns]?.size()} server groups in ${agentType}")

    if (evictableOnDemandCacheDatas) {
      log.info("Evicting onDemand cache keys (${evictableOnDemandCacheDatas.collect { "${it.id}/${start - it.attributes.cacheTime}ms" }.join(", ")})")
    }

    cacheResults[ON_DEMAND.ns].each {
      it.attributes.processedTime = System.currentTimeMillis()
      it.attributes.processedCount = (it.attributes.processedCount ?: 0) + 1
    }

    result
  }

  @Override
  Optional<Map<String, String>> getCacheKeyPatterns() {
    return [
      (SERVER_GROUPS.ns): Keys.getServerGroupKey("*", accountName, "*")
    ]
  }

  @Override
  boolean handles(OnDemandAgent.OnDemandType type, String cloudProvider) {
    type == OnDemandAgent.OnDemandType.ServerGroup && cloudProvider == SpotCloudProvider.ID
  }

  @Override
  OnDemandAgent.OnDemandResult handle(ProviderCache providerCache, Map<String, ? extends Object> data) {
    if (!data.containsKey("serverGroupName")) {
      return null
    }
    if (!data.containsKey("account")) {
      return null
    }
    if (!data.containsKey("region")) {
      return null
    }

    if (account.name != data.account) {
      return null
    }

    if (region != data.region) {
      return null
    }

    String serverGroupName = data.serverGroupName.toString()
    def elastigroupClient = this.account.elastigroupClient
    ElastigroupGetAllRequest getAllElastigroupsRequest = ElastigroupGetAllRequest.Builder.get().setName(serverGroupName).build()
    List<Elastigroup> elastigroups = elastigroupClient.getAllElastigroups(getAllElastigroupsRequest)

    CacheResult result = buildCacheResults(elastigroups)

    def jsonResult = objectMapper.writeValueAsString(result.cacheResults)
    def serverGroupKey = Keys.getServerGroupKey(accountName, serverGroupName, region)
    if (result.cacheResults.values().flatten().isEmpty()) {
      providerCache.evictDeletedItems(
        ON_DEMAND.ns,
        [serverGroupKey]
      )
    } else {
      metricsSupport.onDemandStore {
        def cacheData = new DefaultCacheData(
          serverGroupKey,
          10 * 60, // ttl is 10 minutes.
          [
            cacheTime     : System.currentTimeMillis(),
            cacheResults  : jsonResult,
            processedCount: 0,
            processedTime : null
          ],
          [:]
        )

        providerCache.putCacheData(ON_DEMAND.ns, cacheData)
      }
    }

    Map<String, Collection<String>> evictions = newestElastigroup ? [:] : [(SERVER_GROUPS.ns): [serverGroupKey]]

    log.info "On demand cache refresh (data: ${data}) succeeded."

    return new OnDemandAgent.OnDemandResult(
      sourceAgentType: getOnDemandAgentType(),
      cacheResult: result,
      evictions: evictions
    )
  }

  @Override
  Collection<Map> pendingOnDemandRequests(ProviderCache providerCache) {
    throw new NotImplementedException();
  }


  private List<Elastigroup> loadElastigroups() {
    log.debug("Describing Elastigroups in ${agentType}")
    def elastigroupClient = this.account.elastigroupClient

    ElastigroupGetAllRequest getAllElastigroupsRequest = ElastigroupGetAllRequest.Builder.get().build()
    def elastigroups = elastigroupClient.getAllElastigroups(getAllElastigroupsRequest)

    return elastigroups
  }

  CacheResult buildCacheResults(List<Elastigroup> elastigroups) {
    log.info("Describing items in $agentType")

    Map<String, CacheData> serverGroups = cache()
    Map<String, CacheData> applications = cache()
    Map<String, CacheData> clusters = cache()

    for (Elastigroup elastigroup : elastigroups) {
      try {
        ElastigroupData data = new ElastigroupData(elastigroup, accountName)
        cacheApplication(data, applications)
        cacheCluster(data, clusters)
        cacheServerGroup(data, serverGroups)
      } catch (Exception ex) {
        log.warn("Failed to cache ${elastigroup.name} in ${accountName}/${elastigroup.region}", ex)
      }
    }
    def result = new DefaultCacheResult(
      [
        (APPLICATIONS.ns) : applications.values(),
        (CLUSTERS.ns)     : clusters.values(),
        (SERVER_GROUPS.ns): serverGroups.values()
      ])

    def cacheResults = result.cacheResults

    log.debug("Caching ${cacheResults[CLUSTERS.ns]?.size()} clusters in ${agentType}")
    log.debug("Caching ${cacheResults[SERVER_GROUPS.ns]?.size()} server groups in ${agentType}")

    return result
  }

  private Map<String, CacheData> cache() {
    [:].withDefault { String id -> new MutableCacheData(id) }
  }

  private static class ElastigroupData {
    final Elastigroup elastigroup
    final Names name
    final String appName
    final String cluster
    final String serverGroup

    ElastigroupData(Elastigroup elastigroup, String account) {
      this.elastigroup = elastigroup
      name = Names.parseName(elastigroup.name)
      appName = Keys.getApplicationKey(name.app)
      cluster = Keys.getClusterKey(name.cluster, name.app, account)
      serverGroup = Keys.getServerGroupKey(elastigroup.name, account, elastigroup.region)
    }
  }

  private void cacheApplication(ElastigroupData data, Map<String, CacheData> applications) {
    applications[data.appName].with {
      attributes.name = data.name.app
      relationships[CLUSTERS.ns].add(data.cluster)
      relationships[SERVER_GROUPS.ns].add(data.serverGroup)
    }
  }

  private void cacheCluster(ElastigroupData data, Map<String, CacheData> clusters) {
    clusters[data.cluster].with {
      attributes.name = data.name.cluster
      attributes.application = data.name.app
      relationships[APPLICATIONS.ns].add(data.appName)
      relationships[SERVER_GROUPS.ns].add(data.serverGroup)
    }
  }

  private void cacheServerGroup(ElastigroupData data, Map<String, CacheData> serverGroups) {
    def instances = getElastigroupInstances(data)

    serverGroups[data.serverGroup].with {
      attributes.application = data.name.app
      attributes.elastigroup = objectMapper.convertValue(data.elastigroup, ATTRIBUTES)
      attributes.region = data.elastigroup.region
      attributes.name = data.elastigroup.name
      attributes.cluster = data.name.cluster
      attributes.elastigroupInstances = instances

      relationships[APPLICATIONS.ns].add(data.appName)
      relationships[CLUSTERS.ns].add(data.cluster)
    }
  }

  private List<ElastigroupInstanceHealthiness> getElastigroupInstances(ElastigroupData data) {
    def elastigroupClient = this.account.elastigroupClient
    ElastigroupGetInstanceHealthinessRequest.Builder healthinessRequestBuilder = ElastigroupGetInstanceHealthinessRequest.Builder.get()
    ElastigroupGetInstanceHealthinessRequest healthinessRequest = healthinessRequestBuilder.setElastigroupId(data.elastigroup.id).build()
    List<ElastigroupInstanceHealthiness> instanceHealthinesses = elastigroupClient.getInstanceHealthiness(healthinessRequest)

    return instanceHealthinesses
  }
}
