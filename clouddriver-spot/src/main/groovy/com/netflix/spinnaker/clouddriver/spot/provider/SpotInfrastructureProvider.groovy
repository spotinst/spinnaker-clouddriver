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
package com.netflix.spinnaker.clouddriver.spot.provider

import com.netflix.spinnaker.cats.agent.Agent
import com.netflix.spinnaker.cats.agent.AgentSchedulerAware
import com.netflix.spinnaker.clouddriver.cache.SearchableProvider
import com.netflix.spinnaker.clouddriver.spot.SpotCloudProvider
import com.netflix.spinnaker.clouddriver.spot.cache.Keys
import com.netflix.spinnaker.clouddriver.spot.cache.Keys.Namespace

class SpotInfrastructureProvider extends AgentSchedulerAware implements SearchableProvider {

  final Collection<Agent> agents

  final Set<String> defaultCaches = [
    Namespace.INSTANCES.ns,
    Namespace.SERVER_GROUPS.ns,
  ].asImmutable()

  final Map<String, String> urlMappingTemplates = [:]

  final Map<SearchableProvider.SearchableResource, SearchableProvider.SearchResultHydrator> searchResultHydrators = Collections.emptyMap()

  final String providerName = SpotCloudProvider.ID

  SpotInfrastructureProvider(Collection<Agent> agents) {
    this.agents = agents
  }

  @Override
  Map<String, String> parseKey(String key) {
    return Keys.parse(key)
  }
}
