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
    INSTANCES

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
      default:
        return null
        break
    }

    result
  }

  static String getInstanceKey(String instanceId, String account, String region) {
    "${SpotCloudProvider.ID}:${Namespace.INSTANCES}:${account}:${region}:${instanceId}"
  }

  static String getServerGroupKey(String cluster, String elastigroupName, String account, String region) {
    "${SpotCloudProvider.ID}:${Namespace.SERVER_GROUPS}:${cluster}:${account}:${region}:${elastigroupName}"
  }

  static String getServerGroupKey(String elastigroupName, String account, String region) {
    Names names = Names.parseName(elastigroupName)
    return getServerGroupKey(names.cluster, names.group, account, region)
  }

  static String getApplicationKey(String application) {
    "${SpotCloudProvider.ID}:${Namespace.APPLICATIONS}:${application.toLowerCase()}"
  }

  static String getClusterKey(String clusterName, String application, String account) {
    "${SpotCloudProvider.ID}:${Namespace.CLUSTERS}:${application.toLowerCase()}:${account}:${clusterName}"
  }
}
