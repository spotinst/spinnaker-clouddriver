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
import com.spotinst.sdkjava.enums.ProcessNameEnum
import com.spotinst.sdkjava.model.*
import groovy.transform.CompileStatic
import sun.reflect.generics.reflectiveObjects.NotImplementedException

@CompileStatic
class SpotServerGroup implements ServerGroup, Serializable {

  String name
  String region
  Set health
  Map<String, Object> launchConfig
  Map<String, Object> elastigroup
  List<SpotInstance> elastigroupInstances
  List<ProcessSuspensionResult> suspendedProcesses
  final String type = SpotCloudProvider.ID
  final String cloudProvider = SpotCloudProvider.ID


  private Map<String, Object> dynamicProperties = new HashMap<String, Object>()

  @Override
  String getRegion() {
    if (region == null) {
      ElastigroupComputeConfiguration computeConfiguration = this.elastigroup.compute as ElastigroupComputeConfiguration
      String az = computeConfiguration.availabilityZones.get(0)["azName"]
      region = az.substring(0, az.length() - 1)
    }

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
  Set<Instance> getInstances() {
    Set<Instance> retVal = new HashSet<>()
    Boolean isLbDefined = getLoadBalancers().size() > 0

    List<SpotInstance> spotInstances = new ArrayList<>(elastigroupInstances);

    for (SpotInstance spotInstance : spotInstances) {
      if (false == isLbDefined) {
        spotInstance.setHealthState(HealthState.OutOfService)
      }
    }

    for (SpotInstance spotInstance : spotInstances) {
      retVal.add(spotInstance);
    }

//    retVal.addAll(spotInstances);

    return retVal
  }

  @Override
  Boolean isDisabled() {
    Boolean isLbRegistrationDisabled = false
    Boolean isAutoScaleDisabled = false

    if (suspendedProcesses) {
      for (ProcessSuspensionResult process : suspendedProcesses) {
        def name = process.name

        if (name == ProcessNameEnum.LB_REGISTRATION) {
          isLbRegistrationDisabled = true
        }

        if (name == ProcessNameEnum.AUTO_SCALE) {
          isAutoScaleDisabled = true
        }
      }
    }

    return isLbRegistrationDisabled && isAutoScaleDisabled
  }

  @Override
  Set<String> getZones() {
    Set<String> zones = new HashSet<>()
    ElastigroupComputeConfiguration computeConfiguration = this.elastigroup.compute as ElastigroupComputeConfiguration

    for (LinkedHashMap map : (computeConfiguration.availabilityZones as List<LinkedHashMap>)) {
      zones.add(map.get("azName") as String)
    }

    return zones
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
    List<String> loadBalancerNames = []
    def computeConfiguration = this.elastigroup.compute as ElastigroupComputeConfiguration

    def loadBalancers = computeConfiguration?.launchSpecification?.loadBalancersConfig?.loadBalancers

    if (loadBalancers != null) {
      for (LoadBalancer lb : loadBalancers) {
        loadBalancerNames.add(lb.name)
      }
    }

    return loadBalancerNames.toSet()
  }

  @Override
  Set<String> getSecurityGroups() {
    def computeConfiguration = this.elastigroup.compute as ElastigroupComputeConfiguration
    Set<String> securityGroupIds = new HashSet<>(computeConfiguration.launchSpecification.securityGroupIds)
    return securityGroupIds
  }

  @Override
  InstanceCounts getInstanceCounts() {
    Collection<Instance> instances = getInstances()
    Boolean isLbDefined = getLoadBalancers().size() > 0

    if (isLbDefined) {
      return new InstanceCounts(
        total: instances.size(),
        up: filterInstancesByHealthState(instances, HealthState.Up)?.size() ?: 0,
        down: filterInstancesByHealthState(instances, HealthState.Down)?.size() ?: 0,
        unknown: filterInstancesByHealthState(instances, HealthState.Unknown)?.size() ?: 0,
        starting: 0,
        outOfService: filterInstancesByHealthState(instances, HealthState.OutOfService)?.size() ?: 0)
    } else {
      return new InstanceCounts(
        total: instances.size(),
        up: instances.size(),
        down: 0,
        unknown: filterInstancesByHealthState(instances, HealthState.Unknown)?.size() ?: 0,
        starting: 0,
        outOfService: instances.size())
    }

  }

  static Collection<Instance> filterInstancesByHealthState(Collection<Instance> instances, HealthState healthState) {
    instances.findAll { Instance it -> it.getHealthState() == healthState }
  }

  @Override
  Capacity getCapacity() {

    if (elastigroup && elastigroup.containsKey("capacity")) {
      ElastigroupCapacityConfiguration capacityConfiguration = (ElastigroupCapacityConfiguration) elastigroup.capacity

      return new Capacity(
        min: capacityConfiguration.minimum ? capacityConfiguration.minimum as Integer : 0,
        max: capacityConfiguration.maximum ? capacityConfiguration.maximum as Integer : 0,
        desired: capacityConfiguration.target ? capacityConfiguration.target as Integer : 0
      )
    }
    return null
  }

  @Override
  ImagesSummary getImagesSummary() {
    throw new NotImplementedException()
  }

  @Override
  ImageSummary getImageSummary() {
    throw new NotImplementedException()
  }

}
