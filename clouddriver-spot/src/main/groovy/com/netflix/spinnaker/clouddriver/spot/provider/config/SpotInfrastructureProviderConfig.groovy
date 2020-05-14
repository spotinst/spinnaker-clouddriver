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
package com.netflix.spinnaker.clouddriver.spot.provider.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.netflix.spectator.api.Registry
import com.netflix.spinnaker.cats.agent.Agent
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsRepository
import com.netflix.spinnaker.clouddriver.security.ProviderUtils
import com.netflix.spinnaker.clouddriver.spot.provider.agent.SpotServerGroupCachingAgent
import com.netflix.spinnaker.clouddriver.spot.provider.SpotInfrastructureProvider
import com.netflix.spinnaker.clouddriver.spot.security.SpotAccountCredentials
import com.netflix.spinnaker.config.SpotConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Import

import java.util.concurrent.ConcurrentHashMap

@Configuration
@Import(SpotConfiguration)
@EnableConfigurationProperties
class SpotInfrastructureProviderConfig {

  @Bean
  @DependsOn('spotAccountCredentials')
  SpotInfrastructureProvider spotInfrastructureProvider(String clouddriverUserAgentApplicationName,
                                                        AccountCredentialsRepository accountCredentialsRepository,
                                                        ObjectMapper objectMapper,
                                                        Registry registry) {
    def spotInfrastructureProvider =
      new SpotInfrastructureProvider(Collections.newSetFromMap(new ConcurrentHashMap<Agent, Boolean>()))

    synchronizeSpotInfrastructureProvider(clouddriverUserAgentApplicationName,
      spotInfrastructureProvider,
      accountCredentialsRepository,
      objectMapper,
      registry
    )

    return spotInfrastructureProvider
  }

  private static void synchronizeSpotInfrastructureProvider(
    String clouddriverUserAgentApplicationName,
    SpotInfrastructureProvider spotInfrastructureProvider,
    AccountCredentialsRepository accountCredentialsRepository,
    ObjectMapper objectMapper,
    Registry registry) {
    def scheduledAccounts = ProviderUtils.getScheduledAccounts(spotInfrastructureProvider)
    def allAccounts = ProviderUtils.buildThreadSafeSetOfAccounts(accountCredentialsRepository,
      SpotAccountCredentials)

    objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    allAccounts.each { SpotAccountCredentials credentials ->
      if (!scheduledAccounts.contains(credentials.name)) {
        def newlyAddedAgents = []

        newlyAddedAgents << new SpotServerGroupCachingAgent(credentials,
          objectMapper, registry)

        // If there is an agent scheduler, then this provider has been through the AgentController in the past.
        // In that case, we need to do the scheduling here (because accounts have been added to a running system).
        if (spotInfrastructureProvider.agentScheduler) {
          ProviderUtils.rescheduleAgents(spotInfrastructureProvider, newlyAddedAgents)
        }

        spotInfrastructureProvider.agents.addAll(newlyAddedAgents)
      }
    }
  }
}
