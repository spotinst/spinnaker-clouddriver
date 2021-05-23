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

class ElastigroupUpdateRequestConverter {
  static ElastigroupUpdateRequest toUpdateRequest(Map<String, Object> spinnakerElastigroup) {
    def elastigroup = toElastigroup(spinnakerElastigroup)
    def sdkUpdateRequestBuilder = ElastigroupUpdateRequest.Builder.get()
    sdkUpdateRequestBuilder.setElastigroup(elastigroup)

    def retVal = sdkUpdateRequestBuilder.build()
    retVal
  }

  static Elastigroup toElastigroup(Map<String, Object> spinnakerElastigroup) {
    def sdkElastigroupBuilder = Elastigroup.Builder.get()

    def scaling = spinnakerElastigroup.get("scaling", null)
    if (scaling != null) {
      def sdkScaling = toScaling(scaling as Map<String, Object>)
      sdkElastigroupBuilder.setScaling(sdkScaling)
    }

    def retVal = sdkElastigroupBuilder.build()
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

    def scalingTarget = spinnakerScaling.get("target", null)
    if (scalingTarget != null) {
      def sdkScalingTarget = ((List<Map<String, Object>>)scalingTarget).collect { toScalingPolicy(it) }
      sdkElastigroupScalingConfigurationBuilder.setTarget(sdkScalingTarget)
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

    def target = spinnakerScalingPolicy.get("target", null)
    if (target != null) {
      sdkScalingPolicyBuilder.setTarget(target as Integer)
    }

    def predictive = spinnakerScalingPolicy.get("predictive", null)
    if (predictive != null) {
      def sdkPredictive = toScalingPredictive(predictive as Map<String, Object>)
      sdkScalingPolicyBuilder.setPredictive(sdkPredictive)
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

  static PredictiveScale toScalingPredictive(Map<String, Object> spinnakerScalingPredictive) {
    def sdkScalingPredictiveBuilder = PredictiveScale.Builder.get()

    def mode = spinnakerScalingPredictive.get("mode", null)
    if (mode != null) {
      def modeEnum = ScalingPredictiveModeEnum.fromName(mode as String)
      sdkScalingPredictiveBuilder.setMode(modeEnum)
    }

    def retVal = sdkScalingPredictiveBuilder.build()
    retVal
  }
}
