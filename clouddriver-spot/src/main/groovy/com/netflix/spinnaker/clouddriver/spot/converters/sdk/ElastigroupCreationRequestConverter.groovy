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

package com.netflix.spinnaker.clouddriver.spot.converters.sdk

import com.spotinst.sdkjava.enums.*
import com.spotinst.sdkjava.model.*

class ElastigroupCreationRequestConverter {
  static ElastigroupCreationRequest toCreationRequest(Map<String, Object> spinnakerElastigroup) {
    def elastigroup = toElastigroup(spinnakerElastigroup)
    def sdkCreateRequestBuilder = ElastigroupCreationRequest.Builder.get()
    sdkCreateRequestBuilder.setElastigroup(elastigroup)

    def retVal = sdkCreateRequestBuilder.build()
    retVal
  }

  static Elastigroup toElastigroup(Map<String, Object> spinnakerElastigroup) {
    def sdkElastigroupBuilder = Elastigroup.Builder.get()

    def name = spinnakerElastigroup.get("name", null)
    if (name != null) {
      sdkElastigroupBuilder.setName(name as String)
    }

    def description = spinnakerElastigroup.get("description", null)
    if (description != null) {
      sdkElastigroupBuilder.setDescription(description as String)
    }

    def region = spinnakerElastigroup.get("region", null)
    if (region != null) {
      sdkElastigroupBuilder.setRegion(region as String)
    }

    def capacity = spinnakerElastigroup.get("capacity", null)
    if (capacity != null) {
      def sdkCapacity = toCapacity(capacity as Map<String, Object>)
      sdkElastigroupBuilder.setCapacity(sdkCapacity)
    }

    def strategy = spinnakerElastigroup.get("strategy", null)
    if (strategy != null) {
      def sdkStrategy = toStrategy(strategy as Map<String, Object>)
      sdkElastigroupBuilder.setStrategy(sdkStrategy)
    }

    def compute = spinnakerElastigroup.get("compute", null)
    if (compute != null) {
      def sdkCompute = toCompute(compute as Map<String, Object>)
      sdkElastigroupBuilder.setCompute(sdkCompute)
    }

    def scaling = spinnakerElastigroup.get("scaling", null)
    if (scaling != null) {
      def sdkScaling = toScaling(scaling as Map<String, Object>)
      sdkElastigroupBuilder.setScaling(sdkScaling)
    }

    def thirdPartiesIntegration = spinnakerElastigroup.get("thirdPartiesIntegration", null)
    if (thirdPartiesIntegration != null) {
      def sdkThirdPartiesIntegration = toThirdPartiesIntegration(thirdPartiesIntegration as Map<String, Object>)
      sdkElastigroupBuilder.setThirdPartiesIntegration(sdkThirdPartiesIntegration)
    }

    def scheduling = spinnakerElastigroup.get("scheduling", null)
    if (scheduling != null) {
      def sdkScheduling = toScheduling(scheduling as Map<String, Object>)
      sdkElastigroupBuilder.setScheduling(sdkScheduling)
    }

    def retVal = sdkElastigroupBuilder.build()
    retVal
  }

  static ElastigroupCapacityConfiguration toCapacity(Map<String, Object> spinnakerCapacity) {
    def sdkCapacityBuilder = ElastigroupCapacityConfiguration.Builder.get()

    def minimum = spinnakerCapacity.get("minimum", null)
    if (minimum != null) {
      sdkCapacityBuilder.setMaximum(minimum as Integer)
    }

    def maximum = spinnakerCapacity.get("maximum", null)
    if (maximum != null) {
      sdkCapacityBuilder.setMaximum(maximum as Integer)
    }

    def target = spinnakerCapacity.get("target", null)
    if (target != null) {
      sdkCapacityBuilder.setTarget(target as Integer)
    }

    def unit = spinnakerCapacity.get("unit", null)
    if (unit != null) {
      sdkCapacityBuilder.setUnit(unit as String)
    }

    def retVal = sdkCapacityBuilder.build()
    retVal
  }

  static ElastigroupStrategyConfiguration toStrategy(Map<String, Object> spinnakerStrategy) {
    def sdkStrategyBuilder = ElastigroupStrategyConfiguration.Builder.get()

    def spotPercentage = spinnakerStrategy.get("risk", null)
    if (spotPercentage != null) {
      sdkStrategyBuilder.setSpotPercentage(spotPercentage as Integer)
    }

    def onDemandCount = spinnakerStrategy.get("onDemandCount", null)
    if (onDemandCount != null) {
      sdkStrategyBuilder.setOnDemandCount(onDemandCount as Integer)
    }

    def drainingTimeout = spinnakerStrategy.get("drainingTimeout", null)
    if (drainingTimeout != null) {
      sdkStrategyBuilder.setDrainingTimeout(drainingTimeout as Integer)
    }

    def utilizeReservedInstances = spinnakerStrategy.get("utilizeReservedInstances", null)
    if (utilizeReservedInstances != null) {
      sdkStrategyBuilder.setUtilizeReservedInstances(utilizeReservedInstances as Boolean)
    }

    def fallbackToOd = spinnakerStrategy.get("fallbackToOd", null)
    if (fallbackToOd != null) {
      sdkStrategyBuilder.setFallbackToOnDemand(fallbackToOd as Boolean)
    }

    def elastigroupOrientation = spinnakerStrategy.get("availabilityVsCost", null)
    if (elastigroupOrientation != null) {
      def elastigroupOrientationEnum = ElastigroupOrientationEnum.fromName(elastigroupOrientation as String)
      sdkStrategyBuilder.setElastigroupOrientation(elastigroupOrientationEnum)
    }

    def persistence = spinnakerStrategy.get("persistence", null)
    if (persistence != null) {
      def sdkPersistence = toPersistence(persistence as Map<String, Object>)
      sdkStrategyBuilder.setPersistence(sdkPersistence)
    }

    def retVal = sdkStrategyBuilder.build()
    retVal
  }

  static ElastigroupPersistenceConfiguration toPersistence(Map<String, Object> spinnakerPersistence) {
    def sdkPersistenceBuilder = ElastigroupPersistenceConfiguration.Builder.get()

    def shouldPersistPrivateIp = spinnakerPersistence.get("shouldPersistPrivateIp", null)
    if (shouldPersistPrivateIp != null) {
      sdkPersistenceBuilder.setShouldPersistPrivateIp(shouldPersistPrivateIp as Boolean)
    }

    def shouldPersistBlockDevices = spinnakerPersistence.get("shouldPersistBlockDevices", null)
    if (shouldPersistBlockDevices != null) {
      sdkPersistenceBuilder.setShouldPersistBlockDevices(shouldPersistBlockDevices as Boolean)
    }

    def shouldPersistRootDevice = spinnakerPersistence.get("shouldPersistRootDevice", null)
    if (shouldPersistRootDevice != null) {
      sdkPersistenceBuilder.setShouldPersistRootDevice(shouldPersistRootDevice as Boolean)
    }

    def blockDevicesMode = spinnakerPersistence.get("blockDevicesMode", null)
    if (blockDevicesMode != null) {
      sdkPersistenceBuilder.setBlockDevicesMode(blockDevicesMode as String)
    }

    def retVal = sdkPersistenceBuilder.build()
    retVal
  }

  static ElastigroupComputeConfiguration toCompute(Map<String, Object> spinnakerCompute) {
    def sdkComputeBuilder = ElastigroupComputeConfiguration.Builder.get()

    def product = spinnakerCompute.get("product", null)
    if (product != null) {
      sdkComputeBuilder.setProduct(product as String)
    }

    def launchSpecification = spinnakerCompute.get("launchSpecification", null)
    if (launchSpecification != null) {
      def sdkLaunchSpecification = toLaunchSpecification(launchSpecification as Map<String, Object>)
      sdkComputeBuilder.setLaunchSpecification(sdkLaunchSpecification)
    }

    def instanceTypes = spinnakerCompute.get("instanceTypes", null)
    if (instanceTypes != null) {
      def sdkInstanceTypes = toInstanceTypes(instanceTypes as Map<String, Object>)
      sdkComputeBuilder.setInstanceTypes(sdkInstanceTypes)
    }

    def availabilityZones = spinnakerCompute.get("availabilityZones", null)
    if (availabilityZones != null) {
      def sdkAvailabilityZones = ((List<Map<String, Object>>) availabilityZones).collect { toAvailabilityZone(it) }
      sdkComputeBuilder.setAvailabilityZones(sdkAvailabilityZones)
    }

    def elasticIps = spinnakerCompute.get("elasticIps", null)
    if (elasticIps != null) {
      sdkComputeBuilder.setElasticIps(elasticIps as List<String>)
    }

    def ebsVolumePool = spinnakerCompute.get("ebsVolumePool", null)
    if (spinnakerCompute.containsKey("ebsVolumePool") && spinnakerCompute.ebsVolumePool != null) {
      def sdkEbsVolumePool = ((List<Map<String, Object>>) ebsVolumePool).collect { toEbsVolumePool(it) }
      sdkComputeBuilder.setEbsVolumePools(sdkEbsVolumePool)
    }

    def retVal = sdkComputeBuilder.build()
    retVal
  }

  static ElastigroupLaunchSpecification toLaunchSpecification(Map<String, Object> spinnakerLaunchSpecifications) {
    def sdkLaunchSpecificationBuilder = ElastigroupLaunchSpecification.Builder.get()

    def healthCheckType = spinnakerLaunchSpecifications.get("healthCheckType", null)
    if (healthCheckType != null) {
      sdkLaunchSpecificationBuilder.setHealthCheckType(healthCheckType as String)
    }

    def healthCheckGracePeriod = spinnakerLaunchSpecifications.get("healthCheckGracePeriod", null)
    if (healthCheckGracePeriod != null) {
      sdkLaunchSpecificationBuilder.setHealthCheckGracePeriod(healthCheckGracePeriod as Integer)
    }

    def securityGroupIds = spinnakerLaunchSpecifications.get("securityGroupIds", null)
    if (securityGroupIds != null) {
      sdkLaunchSpecificationBuilder.setSecurityGroupIds(securityGroupIds as List<String>)
    }

    def detailedMonitoring = spinnakerLaunchSpecifications.get("monitoring", null)
    if (detailedMonitoring != null) {
      sdkLaunchSpecificationBuilder.setDetailedMonitoring(detailedMonitoring as Boolean)
    }

    def ebsOptimized = spinnakerLaunchSpecifications.get("ebsOptimized", null)
    if (ebsOptimized != null) {
      sdkLaunchSpecificationBuilder.setEbsOptimized(ebsOptimized as Boolean)
    }

    def imageId = spinnakerLaunchSpecifications.get("imageId", null)
    if (imageId != null) {
      sdkLaunchSpecificationBuilder.setImageId(imageId as String)
    }

    def keyPair = spinnakerLaunchSpecifications.get("keyPair", null)
    if (keyPair != null) {
      sdkLaunchSpecificationBuilder.setKeyPair(keyPair as String)
    }

    def userData = spinnakerLaunchSpecifications.get("userData", null)
    if (userData != null) {
      sdkLaunchSpecificationBuilder.setUserData(userData as String)
    }

    def iamRole = spinnakerLaunchSpecifications.get("iamRole", null)
    if (iamRole != null) {
      def sdkIamRole = toIamRole(iamRole as Map<String, Object>)
      sdkLaunchSpecificationBuilder.setIamRole(sdkIamRole)
    }

    def networkInterfaces = spinnakerLaunchSpecifications.get("networkInterfaces", null)
    if (networkInterfaces != null) {
      def sdkNetworkInterfaces = ((List<Map<String, Object>>) networkInterfaces).collect { toNetworkInterface(it) }
      sdkLaunchSpecificationBuilder.setNetworkInterfaces(sdkNetworkInterfaces)
    }

    def tags = spinnakerLaunchSpecifications.get("tags", null)
    if (tags != null) {
      def sdkTags = ((List<Map<String, Object>>)tags).collect { toTag(it) }
      sdkLaunchSpecificationBuilder.setTags(sdkTags)
    }

    def blockDeviceMappings = spinnakerLaunchSpecifications.get("blockDeviceMappings", null)
    if (blockDeviceMappings != null) {
      def sdkBlockDeviceMappings = ((List<Map<String, Object>>)blockDeviceMappings).collect { toBlockDeviceMapping(it) }
      sdkLaunchSpecificationBuilder.setBlockDeviceMappings(sdkBlockDeviceMappings)
    }

    def loadBalancersConfig = spinnakerLaunchSpecifications.get("loadBalancersConfig", null)
    if (loadBalancersConfig != null) {
      def sdkLoadBalancersConfig = toLoadBalancersConfig(loadBalancersConfig as Map<String, Object>)
      sdkLaunchSpecificationBuilder.setLoadBalancersConfig(sdkLoadBalancersConfig)
    }

    def retVal = sdkLaunchSpecificationBuilder.build()
    retVal
  }

  static IamRole toIamRole(Map<String, Object> spinnakerIamRoles) {
    def sdkIamRoleBuilder = IamRole.Builder.get()

    def name = spinnakerIamRoles.get("name", null)
    if (name != null) {
      sdkIamRoleBuilder.setName(name as String)
    }

    def arn = spinnakerIamRoles.get("arn", null)
    if (arn != null) {
      sdkIamRoleBuilder.setArn(arn as String)
    }

    def retVal = sdkIamRoleBuilder.build()
    retVal
  }

  static NetworkInterface toNetworkInterface(Map<String, Object> spinnakerNetworkInterface) {
    def sdkNetworkInterfaceBuilder = NetworkInterface.Builder.get()

    def description = spinnakerNetworkInterface.get("description", null)
    if (description != null) {
      sdkNetworkInterfaceBuilder.setDescription(description as String)
    }

    def deviceIndex = spinnakerNetworkInterface.get("deviceIndex", null)
    if (deviceIndex != null) {
      sdkNetworkInterfaceBuilder.setDeviceIndex(deviceIndex as Integer)
    }

    def secondaryPrivateIpAddressCount = spinnakerNetworkInterface.get("secondaryPrivateIpAddressCount", null)
    if (secondaryPrivateIpAddressCount != null) {
      sdkNetworkInterfaceBuilder.setSecondaryPrivateIpAddressCount(secondaryPrivateIpAddressCount as Integer)
    }

    def associatePublicIpAddress = spinnakerNetworkInterface.get("associatePublicIpAddress", null)
    if (associatePublicIpAddress != null) {
      sdkNetworkInterfaceBuilder.setAssociatePublicIpAddress(associatePublicIpAddress as Boolean)
    }

    def deleteOnTermination = spinnakerNetworkInterface.get("deleteOnTermination", null)
    if (deleteOnTermination != null) {
      sdkNetworkInterfaceBuilder.setDeleteOnTermination(deleteOnTermination as Boolean)
    }

    def networkInterfaceId = spinnakerNetworkInterface.get("networkInterfaceId", null)
    if (networkInterfaceId != null) {
      sdkNetworkInterfaceBuilder.setNetworkInterfaceId(networkInterfaceId as String)
    }

    def privateIpAddress = spinnakerNetworkInterface.get("privateIpAddress", null)
    if (privateIpAddress != null) {
      sdkNetworkInterfaceBuilder.setPrivateIpAddress(privateIpAddress as String)
    }

    def privateIpAddresses = spinnakerNetworkInterface.get("privateIpAddresses", null)
    if (spinnakerNetworkInterface.containsKey("privateIpAddresses") && spinnakerNetworkInterface.privateIpAddresses != null) {
      def sdkPrivateIpAddresses = ((List<Map<String, Object>>)privateIpAddresses).collect { toPrivateIpAddress(it) }
      sdkNetworkInterfaceBuilder.setPrivateIpAddresses(sdkPrivateIpAddresses)
    }

    def retVal = sdkNetworkInterfaceBuilder.build()
    retVal
  }

  static IpAddress toPrivateIpAddress(Map<String, Object> spinnakerIpAddress) {
    def sdkIpAddressBuilder = IpAddress.Builder.get()

    def privateIpAddress = spinnakerIpAddress.get("privateIpAddress", null)
    if (privateIpAddress != null) {
      sdkIpAddressBuilder.setPrivateIpAddress(privateIpAddress as String)
    }

    def primary = spinnakerIpAddress.get("primary", null)
    if (primary != null) {
      sdkIpAddressBuilder.setPrimary(primary as boolean)
    }

    def retVal = sdkIpAddressBuilder.build()
    retVal
  }

  static Tag toTag(Map<String, Object> spinnakerTags) {
    def sdkTagBuilder = Tag.Builder.get()

    def tagKey = spinnakerTags.get("tagKey", null)
    if (tagKey != null) {
      sdkTagBuilder.setTagKey(tagKey as String)
    }

    def tagValue = spinnakerTags.get("tagValue", null)
    if (tagValue != null) {
      sdkTagBuilder.setTagValue(tagValue as String)
    }

    def retVal = sdkTagBuilder.build()
    retVal
  }

  static BlockDeviceMapping toBlockDeviceMapping(Map<String, Object> spinnakerBlockDeviceMapping) {
    def sdkBlockDeviceMappingBuilder = BlockDeviceMapping.Builder.get()

    def deviceName = spinnakerBlockDeviceMapping.get("deviceName", null)
    if (deviceName != null) {
      sdkBlockDeviceMappingBuilder.setDeviceName(deviceName as String)
    }

    def noDevice = spinnakerBlockDeviceMapping.get("noDevice", null)
    if (noDevice != null) {
      sdkBlockDeviceMappingBuilder.setNoDevice(noDevice as String)
    }

    def virtualName = spinnakerBlockDeviceMapping.get("virtualName", null)
    if (virtualName != null) {
      sdkBlockDeviceMappingBuilder.setVirtualName(virtualName as String)
    }

    def ebsDevice = spinnakerBlockDeviceMapping.get("ebs", null)
    if (ebsDevice != null) {
      def sdkEbsDevice = toEbsDevice(ebsDevice as Map<String, Object>)
      sdkBlockDeviceMappingBuilder.setEbsDevice(sdkEbsDevice)
    }

    def retVal = sdkBlockDeviceMappingBuilder.build()
    retVal
  }

  static EbsDevice toEbsDevice(Map<String, Object> spinnakerEbsDevice) {
    def sdkEbsDeviceBuilder = EbsDevice.Builder.get()

    def deleteOnTermination = spinnakerEbsDevice.get("deleteOnTermination", null)
    if (deleteOnTermination != null) {
      sdkEbsDeviceBuilder.setDeleteOnTermination(deleteOnTermination as Boolean)
    }

    def encrypted = spinnakerEbsDevice.get("encrypted", null)
    if (encrypted != null) {
      sdkEbsDeviceBuilder.setEncrypted(encrypted as Boolean)
    }

    def iops = spinnakerEbsDevice.get("iops", null)
    if (iops != null) {
      sdkEbsDeviceBuilder.setIops(iops as Integer)
    }

    def snapshotId = spinnakerEbsDevice.get("snapshotId", null)
    if (snapshotId != null) {
      sdkEbsDeviceBuilder.setSnapshotId(snapshotId as String)
    }

    def volumeSize = spinnakerEbsDevice.get("volumeSize", null)
    if (volumeSize != null) {
      sdkEbsDeviceBuilder.setVolumeSize(volumeSize as Integer)
    }

    def volumeType = spinnakerEbsDevice.get("volumeType", null)
    if (volumeType != null) {
      def volumeTypeEnum = AwsVolumeTypeEnum.fromName(volumeType as String)
      sdkEbsDeviceBuilder.setVolumeType(volumeTypeEnum)
    }

    def retVal = sdkEbsDeviceBuilder.build()
    retVal
  }

  static LoadBalancersConfig toLoadBalancersConfig(Map<String, Object> spinnakerLoadBalancersConfig) {
    def sdkLoadBalancersConfigBuilder = LoadBalancersConfig.Builder.get()

    def loadBalancers = spinnakerLoadBalancersConfig.get("loadBalancers", null)
    if (loadBalancers != null) {
      def sdkLoadBalancers = ((List<Map<String, Object>>)loadBalancers).collect { toLoadBalancer(it) }
      sdkLoadBalancersConfigBuilder.setLoadBalancers(sdkLoadBalancers)
    }

    def retVal = sdkLoadBalancersConfigBuilder.build()
    retVal
  }

  static LoadBalancer toLoadBalancer(Map<String, Object> spinnakerLoadBalancer) {
    def sdkLoadBalancerBuilder = LoadBalancer.Builder.get()

    def name = spinnakerLoadBalancer.get("name", null)
    if (name != null) {
      sdkLoadBalancerBuilder.setName(name as String)
    }

    def arn = spinnakerLoadBalancer.get("arn", null)
    if (arn != null) {
      sdkLoadBalancerBuilder.setArn(arn as String)
    }

    def type = spinnakerLoadBalancer.get("type", null)
    if (type != null) {
      def typeEnum = LbTypeEnum.fromName(type as String)
      sdkLoadBalancerBuilder.setType(typeEnum)
    }

    def retVal = sdkLoadBalancerBuilder.build()
    retVal
  }

  static ElastigroupInstanceTypes toInstanceTypes(Map<String, Object> spinnakerElastigroupInstanceTypes) {
    def sdkElastigroupInstanceTypesBuilder = ElastigroupInstanceTypes.Builder.get()

    def onDemand = spinnakerElastigroupInstanceTypes.get("ondemand", null)
    if (onDemand != null) {
      sdkElastigroupInstanceTypesBuilder.setOnDemandType(onDemand as String)
    }

    def spot = spinnakerElastigroupInstanceTypes.get("spot", null)
    if (spot != null) {
      sdkElastigroupInstanceTypesBuilder.setSpotTypes(spot as List<String>)
    }

    def retVal = sdkElastigroupInstanceTypesBuilder.build()
    retVal
  }

  static Placement toAvailabilityZone(Map<String, Object> spinnakerAvailabilityZones) {
    def sdkPlacementBuilder = Placement.Builder.get()

    def azName = spinnakerAvailabilityZones.get("name", null)
    if (azName != null) {
      sdkPlacementBuilder.setAvailabilityZoneName(azName as String)
    }

    def subnetIds = spinnakerAvailabilityZones.get("subnetIds", null)
    if (subnetIds != null) {
      sdkPlacementBuilder.setSubnetIds(subnetIds as List<String>)
    }

    def retVal = sdkPlacementBuilder.build()
    retVal
  }

  static ElastigroupEbsVolumePool toEbsVolumePool(Map<String, Object> spinnakerElastigroupEbsVolumePool) {
    def sdkElastigroupEbsVolumePoolBuilder = ElastigroupEbsVolumePool.Builder.get()

    def deviceName = spinnakerElastigroupEbsVolumePool.get("deviceName", null)
    if (deviceName != null) {
      sdkElastigroupEbsVolumePoolBuilder.setDeviceName(deviceName as String)
    }

    def volumeIds = spinnakerElastigroupEbsVolumePool.get("volumeIds", null)
    if (volumeIds != null) {
      sdkElastigroupEbsVolumePoolBuilder.setVolumeIds(volumeIds as List<String>)
    }

    def retVal = sdkElastigroupEbsVolumePoolBuilder.build()
    retVal
  }

  static ElastigroupScalingConfiguration toScaling(Map<String, Object> spinnakerScaling) {
    def sdkElastigroupScalingConfigurationBuilder = ElastigroupScalingConfiguration.Builder.get()

    def scalingUp = spinnakerScaling.get("up", null)
    if (scalingUp != null) {
      def sdkScalingUp = ((List<Map<String, Object>>)scalingUp).collect { toScalingPolicy(it) }
      sdkElastigroupScalingConfigurationBuilder.setUp(sdkScalingUp)
    }

    def scalingdown = spinnakerScaling.get("down", null)
    if (scalingdown != null) {
      def sdkScalingDown = ((List<Map<String, Object>>)scalingdown).collect { toScalingPolicy(it) }
      sdkElastigroupScalingConfigurationBuilder.setDown(sdkScalingDown)
    }

    def retVal = sdkElastigroupScalingConfigurationBuilder.build()
    retVal
  }

  static ScalingPolicy toScalingPolicy(Map<String, Object> spinnakerScalingPolicy) {
    def sdkScalingPolicyBuilder = ScalingPolicy.Builder.get()

    def policyName = spinnakerScalingPolicy.get("policyName", null)
    if (policyName != null) {
      sdkScalingPolicyBuilder.setPolicyName(policyName as String)
    }

    def metricName = spinnakerScalingPolicy.get("metricName", null)
    if (metricName != null) {
      sdkScalingPolicyBuilder.setMetricName(metricName as String)
    }

    def statistic = spinnakerScalingPolicy.get("statistic", null)
    if (statistic != null) {
      sdkScalingPolicyBuilder.setStatistic(statistic as String)
    }

    def extendedStatistic = spinnakerScalingPolicy.get("extendedStatistic", null)
    if (extendedStatistic != null) {
      sdkScalingPolicyBuilder.setExtendedStatistic(extendedStatistic as String)
    }

    def unit = spinnakerScalingPolicy.get("unit", null)
    if (unit != null) {
      sdkScalingPolicyBuilder.setUnit(unit as String)
    }

    def threshold = spinnakerScalingPolicy.get("threshold", null)
    if (threshold != null) {
      sdkScalingPolicyBuilder.setThreshold(threshold as Float)
    }

    def namespace = spinnakerScalingPolicy.get("namespace", null)
    if (namespace != null) {
      sdkScalingPolicyBuilder.setNamespace(namespace as String)
    }

    def period = spinnakerScalingPolicy.get("period", null)
    if (period != null) {
      sdkScalingPolicyBuilder.setPeriod(period as Integer)
    }

    def evaluationPeriods = spinnakerScalingPolicy.get("evaluationPeriods", null)
    if (evaluationPeriods != null) {
      sdkScalingPolicyBuilder.setEvaluationPeriods(evaluationPeriods as Integer)
    }

    def cooldown = spinnakerScalingPolicy.get("cooldown", null)
    if (cooldown != null) {
      sdkScalingPolicyBuilder.setCooldown(cooldown as Integer)
    }

    def dimensions = spinnakerScalingPolicy.get("dimensions", null)
    if (dimensions != null) {
      def sdkDimensions = ((List<Map<String, Object>>)dimensions).collect { toDimension(it) }
      sdkScalingPolicyBuilder.setDimensions(sdkDimensions)
    }

    def operator = spinnakerScalingPolicy.get("operator", null)
    if (operator != null) {
      sdkScalingPolicyBuilder.setOperator(operator as String)
    }

    def action = spinnakerScalingPolicy.get("action", null)
    if (action != null) {
      def sdkAction = toScalingAction(action as Map<String, Object>)
      sdkScalingPolicyBuilder.setAction(sdkAction)
    }

    def isEnabled = spinnakerScalingPolicy.get("isEnabled", null)
    if (isEnabled != null) {
      sdkScalingPolicyBuilder.setIsEnabled(isEnabled as Boolean)
    }

    def retVal = sdkScalingPolicyBuilder.build()
    retVal
  }

  static ScalingDimension toDimension(Map<String, Object> spinnakerScalingDimension) {
    def sdkScalingDimensionBuilder = ScalingDimension.Builder.get()

    def name = spinnakerScalingDimension.get("name", null)
    if (name != null) {
      sdkScalingDimensionBuilder.setName(name as String)
    }

    def value = spinnakerScalingDimension.get("value", null)
    if (value != null) {
      sdkScalingDimensionBuilder.setValue(value as String)
    }

    def retVal = sdkScalingDimensionBuilder.build()
    retVal
  }

  static ScalingAction toScalingAction(Map<String, Object> spinnakerScalingAction) {
    def sdkScalingActionBuilder = ScalingAction.Builder.get()

    def type = spinnakerScalingAction.get("type", null)
    if (type != null) {
      def typeEnum = ScalingActionTypeEnum.fromName(type as String)
      sdkScalingActionBuilder.setType(typeEnum)
    }

    def adjustment = spinnakerScalingAction.get("adjustment", null)
    if (adjustment != null) {
      sdkScalingActionBuilder.setAdjustment(adjustment as String)
    }

    def minTargetCapacity = spinnakerScalingAction.get("minTargetCapacity", null)
    if (minTargetCapacity != null) {
      sdkScalingActionBuilder.setMinTargetCapacity(minTargetCapacity as String)
    }

    def target = spinnakerScalingAction.get("target", null)
    if (target != null) {
      sdkScalingActionBuilder.setTarget(target as String)
    }

    def minimum = spinnakerScalingAction.get("minimum", null)
    if (minimum != null) {
      sdkScalingActionBuilder.setMinimum(minimum as String)
    }

    def maximum = spinnakerScalingAction.get("maximum", null)
    if (maximum != null) {
      sdkScalingActionBuilder.setMaximum(maximum as String)
    }

    def retVal = sdkScalingActionBuilder.build()
    retVal
  }

  static ElastigroupThirdPartiesIntegrationConfiguration toThirdPartiesIntegration(Map<String, Object> spinnaker3rdPartyIntegration) {
    def sdk3rdPartiesIntegration = ElastigroupThirdPartiesIntegrationConfiguration.Builder.get()

    def ecs = spinnaker3rdPartyIntegration.get("ecs", null)
    if (ecs != null) {
      def sdkEcs = toEcsIntegration(ecs as Map<String, Object>)
      sdk3rdPartiesIntegration.setEcs(sdkEcs)
    }

    def retVal = sdk3rdPartiesIntegration.build()
    retVal
  }

  static ElastigroupEcsSpecification toEcsIntegration(Map<String, Object> spinnakerEcsIntegration) {
    def sdkElastigroupEcsSpecificationBuilder = ElastigroupEcsSpecification.Builder.get()

    def clusterName = spinnakerEcsIntegration.get("clusterName", null)
    if (clusterName != null) {
      sdkElastigroupEcsSpecificationBuilder.setClusterName(clusterName as String)
    }

    def autoScale = spinnakerEcsIntegration.get("autoScale", null)
    if (autoScale != null) {
      def sdkAutoScale = toEcsAutoScale(autoScale as Map<String, Object>)
      sdkElastigroupEcsSpecificationBuilder.setAutoScale(sdkAutoScale)
    }

    def optimizeImages = spinnakerEcsIntegration.get("optimizeImages", null)
    if (optimizeImages != null) {
      def sdkOptimizeImages = toEcsOptimizeImages(optimizeImages as Map<String, Object>)
      sdkElastigroupEcsSpecificationBuilder.setOptimizeImages(sdkOptimizeImages)
    }

    def batch = spinnakerEcsIntegration.get("batch", null)
    if (batch != null) {
      def sdkBatch = toEcsBatch(batch as Map<String, Object>)
      sdkElastigroupEcsSpecificationBuilder.setBatch(sdkBatch)
    }

    def retVal = sdkElastigroupEcsSpecificationBuilder.build()
    retVal
  }

  static ElastigroupAutoScaleSpecification toEcsAutoScale(Map<String, Object> spinnakerEcsAutoScale) {
    def sdkElastigroupAutoScaleSpecificationBuilder = ElastigroupAutoScaleSpecification.Builder.get()

    def isEnabled = spinnakerEcsAutoScale.get("isEnabled", null)
    if (isEnabled != null) {
      sdkElastigroupAutoScaleSpecificationBuilder.setIsEnabled(isEnabled as Boolean)
    }

    def cooldown = spinnakerEcsAutoScale.get("cooldown", null)
    if (cooldown != null) {
      sdkElastigroupAutoScaleSpecificationBuilder.setCooldown(cooldown as Integer)
    }

    def down = spinnakerEcsAutoScale.get("down", null)
    if (down != null) {
      def sdkDown = toEcsDown(down as Map<String, Object>)
      sdkElastigroupAutoScaleSpecificationBuilder.setDown(sdkDown)
    }

    def headroom = spinnakerEcsAutoScale.get("headroom", null)
    if (headroom != null) {
      def sdkHeadroom = toEcsHeadroom(headroom as Map<String, Object>)
      sdkElastigroupAutoScaleSpecificationBuilder.setHeadroom(sdkHeadroom)
    }

    def isAutoConfig = spinnakerEcsAutoScale.get("isAutoConfig", null)
    if (isAutoConfig != null) {
      sdkElastigroupAutoScaleSpecificationBuilder.setIsAutoConfig(isAutoConfig as Boolean)
    }

    def shouldScaleDownNonServiceTasks = spinnakerEcsAutoScale.get("shouldScaleDownNonServiceTasks", null)
    if (shouldScaleDownNonServiceTasks != null) {
      sdkElastigroupAutoScaleSpecificationBuilder.setShouldScaleDownNonServiceTasks(shouldScaleDownNonServiceTasks as Boolean)
    }

    def attributes = spinnakerEcsAutoScale.get("attributes", null)
    if (attributes != null) {
      def sdkAttributes = ((List<Map<String, Object>>)attributes).collect { toEcsAttribute(it) }
      sdkElastigroupAutoScaleSpecificationBuilder.setAttributes(sdkAttributes)
    }

    def retVal = sdkElastigroupAutoScaleSpecificationBuilder.build()
    retVal
  }

  static ElastigroupDownSpecification toEcsDown(Map<String, Object> spinnakerEcsDown) {
    def sdkElastigroupDownSpecificationBuilder = ElastigroupDownSpecification.Builder.get()

    def evaluationPeriods = spinnakerEcsDown.get("evaluationPeriods", null)
    if (evaluationPeriods != null) {
      sdkElastigroupDownSpecificationBuilder.setEvaluationPeriods(evaluationPeriods as Integer)
    }

    def maxScaleDownPercentage = spinnakerEcsDown.get("maxScaleDownPercentage", null)
    if (maxScaleDownPercentage != null) {
      sdkElastigroupDownSpecificationBuilder.setMaxScaleDownPercentage(maxScaleDownPercentage as Integer)
    }

    def retVal = sdkElastigroupDownSpecificationBuilder.build()
    retVal
  }

  static ElastigroupHeadroomSpecification toEcsHeadroom(Map<String, Object> spinnakerEcsHeadroom) {
    def sdkElastigroupHeadroomSpecificationBuilder = ElastigroupHeadroomSpecification.Builder.get()

    def cpuPerUnit = spinnakerEcsHeadroom.get("cpuPerUnit", null)
    if (cpuPerUnit != null) {
      sdkElastigroupHeadroomSpecificationBuilder.setCpuPerUnit(cpuPerUnit as Integer)
    }

    def memoryPerUnit = spinnakerEcsHeadroom.get("memoryPerUnit", null)
    if (memoryPerUnit != null) {
      sdkElastigroupHeadroomSpecificationBuilder.setMemoryPerUnit(memoryPerUnit as Integer)
    }

    def numOfUnits = spinnakerEcsHeadroom.get("numOfUnits", null)
    if (numOfUnits != null) {
      sdkElastigroupHeadroomSpecificationBuilder.setNumOfUnits(numOfUnits as Integer)
    }

    def retVal = sdkElastigroupHeadroomSpecificationBuilder.build()
    retVal
  }

  static ElastigroupAttributesSpecification toEcsAttribute(Map<String, Object> spinnakerEcsAttribute) {
    def sdkElastigroupAttributesSpecificationBuilder = ElastigroupAttributesSpecification.Builder.get()

    def key = spinnakerEcsAttribute.get("key", null)
    if (key != null) {
      sdkElastigroupAttributesSpecificationBuilder.setkey(key as String)
    }

    def value = spinnakerEcsAttribute.get("value", null)
    if (value != null) {
      sdkElastigroupAttributesSpecificationBuilder.setValue(value as String)
    }

    def retVal = sdkElastigroupAttributesSpecificationBuilder.build()
    retVal
  }

  static ElastigroupOptimizeImages toEcsOptimizeImages(Map<String, Object> spinnakerEcsOptimizeImages) {
    def sdkElastigroupOptimizeImagesBuilder = ElastigroupOptimizeImages.Builder.get()

    def shouldOptimizeEcsAmi = spinnakerEcsOptimizeImages.get("shouldOptimizeEcsAmi", null)
    if (shouldOptimizeEcsAmi != null) {
      sdkElastigroupOptimizeImagesBuilder.setShouldOptimizeEcsAmi(shouldOptimizeEcsAmi as Boolean)
    }

    def performAt = spinnakerEcsOptimizeImages.get("performAt", null)
    if (performAt != null) {
      def performAtEnum = MaintenanceWindowTypeEnum.fromName(performAt as String)
      sdkElastigroupOptimizeImagesBuilder.setPerformAt(performAtEnum)
    }

    def timeWindows = spinnakerEcsOptimizeImages.get("timeWindows", null)
    if (timeWindows != null) {
      sdkElastigroupOptimizeImagesBuilder.setTimeWindow(timeWindows as List<String>)
    }

    def retVal = sdkElastigroupOptimizeImagesBuilder.build()
    retVal
  }

  static ElastigroupEcsBatch toEcsBatch(Map<String, Object> spinnakerEcsBatch) {
    def sdkElastigroupEcsBatchBuilder = ElastigroupEcsBatch.Builder.get()

    def jobQueueNames = spinnakerEcsBatch.get("jobQueueNames", null)
    if (jobQueueNames != null) {
      sdkElastigroupEcsBatchBuilder.setJobQueueNames(jobQueueNames as List<String>)
    }

    def retVal = sdkElastigroupEcsBatchBuilder.build()
    retVal
  }

  static ElastigroupSchedulingConfiguration toScheduling(Map<String, Object> spinnakerScheduling) {
    def sdkElastigroupSchedulingConfigurationBuilder = ElastigroupSchedulingConfiguration.Builder.get()

    def tasks = spinnakerScheduling.get("tasks", null)
    if (tasks != null) {
      def sdkTasks = ((List<Map<String, Object>>)tasks).collect { toSchedulingTask(it) }
      sdkElastigroupSchedulingConfigurationBuilder.setTasks(sdkTasks)
    }

    def retVal = sdkElastigroupSchedulingConfigurationBuilder.build()
    retVal
  }

  static TasksConfiguration toSchedulingTask(Map<String, Object> spinnakerSchedulingTask) {
    def sdkTasksConfigurationBuilder = TasksConfiguration.Builder.get()

    def isEnabled = spinnakerSchedulingTask.get("isEnabled", null)
    if (isEnabled != null) {
      sdkTasksConfigurationBuilder.setIsEnabled(isEnabled as Boolean)
    }

    def frequency = spinnakerSchedulingTask.get("frequency", null)
    if (frequency != null) {
      def frequencyEnum = RecurrenceFrequencyEnum.fromName(frequency as String)
      sdkTasksConfigurationBuilder.setFrequency(frequencyEnum)
    }

    def startTime = spinnakerSchedulingTask.get("startTime", null)
    if (startTime != null) {
      sdkTasksConfigurationBuilder.setStartTime(startTime as Date)
    }

    def cronExpression = spinnakerSchedulingTask.get("cronExpression", null)
    if (cronExpression != null) {
      sdkTasksConfigurationBuilder.setCronExpression(cronExpression as String)
    }

    def taskType = spinnakerSchedulingTask.get("taskType", null)
    if (taskType != null) {
      def taskTypeEnum = SchedulingTaskTypeEnum.fromName(taskType as String)
      sdkTasksConfigurationBuilder.setTaskType(taskTypeEnum)
    }

    def scaleTargetCapacity = spinnakerSchedulingTask.get("scaleTargetCapacity", null)
    if (scaleTargetCapacity != null) {
      sdkTasksConfigurationBuilder.setScaleTargetCapacity(scaleTargetCapacity as Integer)
    }

    def scaleMinCapacity = spinnakerSchedulingTask.get("scaleMinCapacity", null)
    if (scaleMinCapacity != null) {
      sdkTasksConfigurationBuilder.setScaleMinCapacity(scaleMinCapacity as Integer)
    }

    def scaleMaxCapacity = spinnakerSchedulingTask.get("scaleMaxCapacity", null)
    if (scaleMaxCapacity != null) {
      sdkTasksConfigurationBuilder.setScaleMaxCapacity(scaleMaxCapacity as Integer)
    }

    def batchSizePercentage = spinnakerSchedulingTask.get("batchSizePercentage", null)
    if (batchSizePercentage != null) {
      sdkTasksConfigurationBuilder.setBatchSizePercentage(batchSizePercentage as Integer)
    }

    def gracePeriod = spinnakerSchedulingTask.get("gracePeriod", null)
    if (gracePeriod != null) {
      sdkTasksConfigurationBuilder.setGracePeriod(gracePeriod as Integer)
    }

    def adjustment = spinnakerSchedulingTask.get("adjustment", null)
    if (adjustment != null) {
      sdkTasksConfigurationBuilder.setAdjustment(adjustment as Integer)
    }

    def adjustmentPercentage = spinnakerSchedulingTask.get("adjustmentPercentage", null)
    if (adjustmentPercentage != null) {
      sdkTasksConfigurationBuilder.setAdjustmentPercentage(adjustmentPercentage as Integer)
    }

    def targetCapacity = spinnakerSchedulingTask.get("targetCapacity", null)
    if (targetCapacity != null) {
      sdkTasksConfigurationBuilder.setTargetCapacity(targetCapacity as Integer)
    }

    def minCapacity = spinnakerSchedulingTask.get("minCapacity", null)
    if (minCapacity != null) {
      sdkTasksConfigurationBuilder.setMinCapacity(minCapacity as Integer)
    }

    def maxCapacity = spinnakerSchedulingTask.get("maxCapacity", null)
    if (maxCapacity != null) {
      sdkTasksConfigurationBuilder.setMaxCapacity(maxCapacity as Integer)
    }

    def retVal = sdkTasksConfigurationBuilder.build()
    retVal
  }
}
