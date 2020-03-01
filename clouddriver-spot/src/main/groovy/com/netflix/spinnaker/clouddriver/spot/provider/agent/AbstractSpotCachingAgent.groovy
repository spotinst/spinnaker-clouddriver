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
import com.fasterxml.jackson.databind.ser.FilterProvider
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import com.netflix.spinnaker.cats.agent.CachingAgent
import com.netflix.spinnaker.clouddriver.spot.SpotCloudProvider
import com.netflix.spinnaker.clouddriver.spot.security.SpotAccountCredentials

abstract class AbstractSpotCachingAgent implements CachingAgent {

  final TypeReference<Map<String, Object>> ATTRIBUTES = new TypeReference<Map<String, Object>>() {}
  final String clouddriverUserAgentApplicationName // "Spinnaker/${version}" HTTP header string see CloudDriverConfig
  final SpotAccountCredentials credentials
  final ObjectMapper objectMapper
  final String providerName = SpotCloudProvider.ID
  final String agentType


  AbstractSpotCachingAgent(ObjectMapper objectMapper, SpotAccountCredentials credentials, String clouddriverUserAgentApplicationName) {
    this.credentials = credentials
    this.clouddriverUserAgentApplicationName = clouddriverUserAgentApplicationName
    agentType = "${credentials.name}/${credentials.region}/${this.class.simpleName}"

    FilterProvider filters = new SimpleFilterProvider().setFailOnUnknownId(false)
    //Alternatives of adding explicitlySetFilter:
    //- FilterProvider filters = new SimpleFilterProvider().addFilter("explicitlySetFilter", (SimpleBeanPropertyFilter) SimpleBeanPropertyFilter.serializeAllExcept(['__explicitlySet__'].toSet()));
    //- FilterProvider filters = new SimpleFilterProvider().addFilter("explicitlySetFilter", (SimpleBeanPropertyFilter) com.spot.bmc.http.internal.ExplicitlySetFilter.INSTANCE)
    this.objectMapper = objectMapper.setFilterProvider(filters)
  }
}
