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
package com.netflix.spinnaker.config

import com.netflix.spinnaker.clouddriver.spot.config.SpotConfigurationProperties
import com.netflix.spinnaker.clouddriver.spot.model.SpotServerGroup
import com.netflix.spinnaker.clouddriver.spot.provider.view.SpotClusterProvider
import com.netflix.spinnaker.clouddriver.spot.security.SpotCredentialsInitializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableConfigurationProperties
@EnableScheduling
@ConditionalOnProperty('spot.enabled')
@ComponentScan(["com.netflix.spinnaker.clouddriver.spot"])
@Import([SpotCredentialsInitializer])
class SpotConfiguration {

  @Bean
  @ConfigurationProperties("spot")
  SpotConfigurationProperties spotConfigurationProperties() {
    new SpotConfigurationProperties()
  }


  class SpotServerGroupProvider {
    ApplicationContext applicationContext

    SpotServerGroupProvider(ApplicationContext applicationContext) {
      this.applicationContext = applicationContext
    }

    SpotServerGroup getServerGroup(String account, String region, String serverGroupName) {
      return applicationContext.getBean(SpotClusterProvider).getServerGroup(account, region, serverGroupName)
    }
  }

}
