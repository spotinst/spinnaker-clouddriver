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

package com.netflix.spinnaker.clouddriver.spot.security

import com.netflix.spinnaker.cats.module.CatsModule
import com.netflix.spinnaker.clouddriver.spot.config.SpotConfigurationProperties
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsRepository
import com.netflix.spinnaker.clouddriver.security.ProviderUtils
import groovy.util.logging.Slf4j
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Slf4j
@Configuration
class SpotCredentialsInitializer {

  @Bean
  List<? extends SpotAccountCredentials> spotAccountCredentials(
    String clouddriverUserAgentApplicationName, //todo yossi see CloudDriverConfig.clouddriverUserAgentApplicationName
    SpotConfigurationProperties spotConfigurationProperties,
    AccountCredentialsRepository accountCredentialsRepository
  ) {
    synchronizeSpotAccounts(clouddriverUserAgentApplicationName, spotConfigurationProperties, null, accountCredentialsRepository)
  }

  private List<? extends SpotAccountCredentials> synchronizeSpotAccounts(
    String clouddriverUserAgentApplicationName,
    SpotConfigurationProperties spotConfigurationProperties,
    CatsModule catsModule,
    AccountCredentialsRepository accountCredentialsRepository) {

    def (ArrayList<SpotConfigurationProperties.ManagedAccount> accountsToAdd, List<String> namesOfDeletedAccounts) =
    ProviderUtils.calculateAccountDeltas(accountCredentialsRepository,
      SpotAccountCredentials.class,
      spotConfigurationProperties.accounts)

    accountsToAdd.each { SpotConfigurationProperties.ManagedAccount managedAccount ->
      try {
        def spotAccount = new SpotAccountCredentials.Builder().name(managedAccount.name).
          environment(managedAccount.environment ?: managedAccount.name).
          accountType(managedAccount.accountType ?: managedAccount.name).
          requiredGroupMembership(managedAccount.requiredGroupMembership).
          accountId(managedAccount.accountId).
          build()

        accountCredentialsRepository.save(managedAccount.name, spotAccount)
      } catch (e) {
        log.warn("Could not load account $managedAccount.name for Spot", e)
      }
    }

    ProviderUtils.unscheduleAndDeregisterAgents(namesOfDeletedAccounts, catsModule)

    accountCredentialsRepository.all.findAll {
      it instanceof SpotAccountCredentials
    } as List<SpotAccountCredentials>
  }
}
