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

package com.netflix.spinnaker.clouddriver.spot.model

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.netflix.spinnaker.clouddriver.model.HealthState
import com.netflix.spinnaker.clouddriver.model.Instance
import com.netflix.spinnaker.clouddriver.spot.SpotCloudProvider
import com.spotinst.sdkjava.enums.InstanceHealthStatusEnum

class SpotInstance implements Instance, Serializable {

  String name
  Long launchTime
  HealthState healthState
  String zone
  List<Map<String, Object>> health = []
  final String providerType = SpotCloudProvider.ID
  final String cloudProvider = SpotCloudProvider.ID

  private Map<String, Object> dynamicProperties = new HashMap<String, Object>()

  @JsonAnyGetter
  Map<String, Object> any() {
    return dynamicProperties;
  }

  @JsonAnySetter
  void set(String name, Object value) {
    dynamicProperties.put(name, value);
  }

  @Override
  List<Map<String, Object>> getHealth() {
    Map<String, Object> health = new HashMap<>()
    health.put("healthClass", "platform")
    health.put("state", (healthState == null ? HealthState.Unknown : healthState).toString())
    return Collections.singletonList(health)
  }

  static HealthState convertSpotHealthStatusToHealthState(InstanceHealthStatusEnum instanceHealthStatusEnum) {
    def retVal
    switch (instanceHealthStatusEnum) {
      case InstanceHealthStatusEnum.HEALTHY:
        retVal = HealthState.Up
        break
      case InstanceHealthStatusEnum.UNHEALTHY:
        retVal = HealthState.OutOfService
        break
      case InstanceHealthStatusEnum.INSUFFICIENT_DATA:
        retVal = HealthState.Unknown
        break
      case InstanceHealthStatusEnum.UNKNOWN:
        retVal = HealthState.Unknown
    }

    return retVal
  }

  @Override
  boolean equals(o) {
    if (this.is(o)) return true
    if (getClass() != o.class) return false

    SpotInstance that = (SpotInstance) o

    if (cloudProvider != that.cloudProvider) return false
    if (health != that.health) return false
    if (launchTime != that.launchTime) return false
    if (name != that.name) return false
    if (providerType != that.providerType) return false

    return true
  }

  @Override
  int hashCode() {
    int result
    result = (name != null ? name.hashCode() : 0)
    result = 31 * result + (launchTime != null ? launchTime.hashCode() : 0)
    result = 31 * result + (health != null ? health.hashCode() : 0)
    result = 31 * result + (providerType != null ? providerType.hashCode() : 0)
    result = 31 * result + (cloudProvider != null ? cloudProvider.hashCode() : 0)
    return result
  }
}
