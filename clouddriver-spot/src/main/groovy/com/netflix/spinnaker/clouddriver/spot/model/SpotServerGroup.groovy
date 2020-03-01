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
import com.netflix.spinnaker.clouddriver.model.ServerGroup
import com.netflix.spinnaker.clouddriver.spot.SpotCloudProvider
import com.spotinst.sdkjava.model.ElastigroupCapacityConfiguration
import groovy.transform.CompileStatic

@CompileStatic
class SpotServerGroup implements ServerGroup, Serializable {

  String name
  String region
  Set<String> zones
  Set<Instance> instances
  Set health
  Map<String, Object> image
  Map<String, Object> launchConfig
  Map<String, Object> elastigroup
  Map buildInfo
  String vpcId
  final String type = SpotCloudProvider.ID
  final String cloudProvider = SpotCloudProvider.ID

  Set<String> targetGroups

  private Map<String, Object> dynamicProperties = new HashMap<String, Object>()

  @Override
  String getRegion() {
    return region
  }

  @JsonAnyGetter
  Map<String, Object> any() {
    return dynamicProperties
  }

  @JsonAnySetter
  void set(String name, Object value) {
    dynamicProperties.put(name, value)
  }

  @Override
  Boolean isDisabled() {
    return false
  }

  @Override
  Long getCreatedTime() {
    if (!elastigroup) {
      return null
    }
    (Long) elastigroup.createdAt
  }

  @Override
  Set<String> getLoadBalancers() {
    Set<String> loadBalancerNames = []
    def elastigroup = getElastigroup()
    if (elastigroup && elastigroup.containsKey("loadBalancerNames")) {
      loadBalancerNames = (Set<String>) elastigroup.loadBalancerNames
    }

    return loadBalancerNames
  }

  void setTargetGroups() {
    Set<String> targetGroupNames = []
    def elastigroup = getElastigroup()
    if (elastigroup && elastigroup.containsKey("targetGroupNames")) {
      targetGroupNames = (Set<String>) elastigroup.targetGroupNames
    }
    this.targetGroups = targetGroupNames
  }

  @Override
  Set<String> getSecurityGroups() {
    Set<String> securityGroups = []
    if (launchConfig && launchConfig.containsKey("securityGroups")) {
      securityGroups = (Set<String>) launchConfig.securityGroups
    }
    securityGroups
  }

  @Override
  ServerGroup.InstanceCounts getInstanceCounts() {
    new ServerGroup.InstanceCounts(
      total: 0,
      up: 0,
      down: 0,
      unknown: 0,
      starting: 0,
      outOfService: 0)
  }

  @Override
  ServerGroup.Capacity getCapacity() {

    if (elastigroup && elastigroup.containsKey("capacity")) {
      ElastigroupCapacityConfiguration capacityConfiguration  = (ElastigroupCapacityConfiguration) elastigroup.capacity

      return new ServerGroup.Capacity(
        min: capacityConfiguration.minimum ? capacityConfiguration.minimum as Integer : 0,
        max: capacityConfiguration.maximum ? capacityConfiguration.maximum as Integer : 0,
        desired: capacityConfiguration.target ? capacityConfiguration.target as Integer : 0
      )
    }
    return null
  }

  @Override
  ServerGroup.ImagesSummary getImagesSummary() {
    def i = image
    def bi = buildInfo
    return new ServerGroup.ImagesSummary() {
      @Override
      List<? extends ServerGroup.ImageSummary> getSummaries() {
        return [new ServerGroup.ImageSummary() {
          String serverGroupName = name
          String imageName = i?.name
          String imageId = i?.imageId

          @Override
          Map<String, Object> getBuildInfo() {
            return bi
          }

          @Override
          Map<String, Object> getImage() {
            return i
          }
        }]
      }
    }
  }

  @Override
  ServerGroup.ImageSummary getImageSummary() {
    imagesSummary?.summaries?.get(0)
  }

  static Collection<Instance> filterInstancesByHealthState(Collection<Instance> instances, HealthState healthState) {
    instances.findAll { Instance it -> it.getHealthState() == healthState }
  }

}
