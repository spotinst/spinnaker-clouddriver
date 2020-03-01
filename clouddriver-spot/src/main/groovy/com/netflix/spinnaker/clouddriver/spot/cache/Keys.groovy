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
package com.netflix.spinnaker.clouddriver.spot.cache

import com.netflix.frigga.Names
import com.netflix.spinnaker.clouddriver.spot.SpotCloudProvider
import groovy.util.logging.Slf4j

@Slf4j
class Keys {

  static enum Namespace {

    APPLICATIONS,
    SERVER_GROUPS,
    CLUSTERS,
    NETWORKS,
    SUBNETS,
    IMAGES,
    SECURITY_GROUPS,
    INSTANCES,
    LOADBALANCERS

    static String provider = SpotCloudProvider.ID

    final String ns

    private Namespace() {

      def parts = name().split('_')

      ns = parts.tail().inject(new StringBuilder(parts.head().toLowerCase())) { val, next -> val.append(next.charAt(0)).append(next.substring(1).toLowerCase()) }
    }

    String toString() {
      ns
    }
  }

  static Map<String, String> parse(String key) {
    def parts = key.split(':')

    if (parts.length < 2 || parts[0] != SpotCloudProvider.ID) {
      return null
    }

    def result = [provider: parts[0], type: parts[1]]

    if (result.provider != Namespace.provider) {
      return null
    }

    switch (result.type) {
      case Namespace.CLUSTERS.ns:
        def names = Names.parseName(parts[4])
        result << [application: parts[2].toLowerCase(), account: parts[3], cluster: parts[4], stack: names.stack, detail: names.detail]
        break
      case Namespace.INSTANCES.ns:
        result << [
          account: parts[2],
          region : parts[3],
          name   : parts[4],
          id     : parts[5]
        ]
        break
      case Namespace.NETWORKS.ns:
        result << [
          name   : parts[2],
          id     : parts[3],
          region : parts[4],
          account: parts[5]
        ]
        break
      case Namespace.SUBNETS.ns:
        result << [
          id     : parts[2],
          region : parts[3],
          account: parts[4]
        ]
        break
      case Namespace.SERVER_GROUPS.ns:
        def names = Names.parseName(parts[5])
        result << [
          application: names.app,
          cluster    : parts[2],
          account    : parts[3],
          region     : parts[4],
          stack      : names.stack,
          detail     : names.detail,
          serverGroup: parts[5],
          name       : parts[5]
        ]
        break
      case Namespace.IMAGES.ns:
        result << [
          account: parts[2],
          region : parts[3],
          imageId: parts[4]
        ]
        break
      case Namespace.SECURITY_GROUPS.ns:
        def names = Names.parseName(parts[2])
        result << [
          application: names.app,
          name       : parts[2],
          id         : parts[3],
          region     : parts[4],
          account    : parts[5]
        ]
        break
      case Namespace.LOADBALANCERS.ns:
        result << [
          name   : parts[2],
          id     : parts[3],
          region : parts[4],
          account: parts[5]
        ]
        break
      default:
        return null
        break
    }

    result
  }

  static String getSecurityGroupKey(String securityGroupName,
                                    String securityGroupId,
                                    String region,
                                    String account) {
    "$SpotCloudProvider.ID:${Namespace.SECURITY_GROUPS}:${securityGroupName}:${securityGroupId}:${region}:${account}"
  }

  static String getImageKey(String account, String region, String imageId) {
    "$SpotCloudProvider.ID:${Namespace.IMAGES}:${account}:${region}:${imageId}"
  }

  static String getInstanceKey(String account,
                               String region,
                               String name,
                               String id) {
    "$SpotCloudProvider.ID:${Namespace.INSTANCES}:${account}:${region}:${name}:${id}"
  }

  static String getNetworkKey(String networkName,
                              String networkId,
                              String region,
                              String account) {
    "$SpotCloudProvider.ID:${Namespace.NETWORKS}:${networkName}:${networkId}:${region}:${account}"
  }

  static String getSubnetKey(String subnetId,
                             String region,
                             String account) {
    "$SpotCloudProvider.ID:${Namespace.SUBNETS}:${subnetId}:${region}:${account}"
  }

  static String getServerGroupKey(String cluster, String elastigroupName, String account, String region) {
    "${SpotCloudProvider.ID}:${Namespace.SERVER_GROUPS}:${cluster}:${account}:${region}:${elastigroupName}"
  }

  static String getServerGroupKey(String elastigroupName, String account, String region) {
    Names names = Names.parseName(elastigroupName)
    return getServerGroupKey(names.cluster, names.group, account, region)
  }

  static String getLoadBalancerKey(String loadBalancerName,
                                   String loadBalancerId,
                                   String region,
                                   String account) {
    "$SpotCloudProvider.ID:${Namespace.NETWORKS}:${loadBalancerName}:${loadBalancerId}:${region}:${account}"
  }

  static String getApplicationKey(String application) {
    "${SpotCloudProvider.ID}:${Namespace.APPLICATIONS}:${application.toLowerCase()}"
  }

  static String getClusterKey(String clusterName, String application, String account) {
    "${SpotCloudProvider.ID}:${Namespace.CLUSTERS}:${application.toLowerCase()}:${account}:${clusterName}"
  }
}
