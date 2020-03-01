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


import com.netflix.spinnaker.clouddriver.security.AccountCredentials
import com.netflix.spinnaker.clouddriver.spot.SpotCloudProvider
import com.spotinst.sdkjava.model.SpotinstElastigroupClient

class SpotAccountCredentials implements AccountCredentials<Object> {

  String cloudProvider = SpotCloudProvider.ID

  String name
  String accountId
  SpotinstElastigroupClient elastigroupClient

  SpotAccountCredentials(String name, String accountId) {
    this.name = name
    this.accountId = accountId
    String auth_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzcG90aW5zdCIsImV4cCI6MTgyNDM3NjIxNCwidWlkIjotMSwicm9sZSI6Miwib2lkIjo2MDYwNzk4NjIyNjEsImlhdCI6MTUwOTAxNjIxNH0.ptsB3-cUeIVflzeJnSfJDV2YeDXrPfooJbisFcuGVx0"
    this.elastigroupClient = new SpotinstElastigroupClient(auth_token, accountId);

  }

  @Override
  String getEnvironment() {
    return "production"
  }

  @Override
  String getAccountType() {
    return "test"
  }

  @Override
  Object getCredentials() {
    return null
  }

  @Override
  List<String> getRequiredGroupMembership() {
    return null
  }

  static class Builder {

    String name
    String environment
    String accountType
    List<String> requiredGroupMembership = []
    String accountId

    Builder name(String name) {
      this.name = name
      return this
    }

    Builder environment(String environment) {
      this.environment = environment
      return this
    }

    Builder accountType(String accountType) {
      this.accountType = accountType
      return this
    }

    Builder requiredGroupMembership(List<String> requiredGroupMembership) {
      this.requiredGroupMembership = requiredGroupMembership
      return this
    }

    Builder accountId(String accountId) {
      this.accountId = accountId
      return this
    }

    SpotAccountCredentials build() {
      return new SpotAccountCredentials(
        this.name,
        this.accountId)
    }
  }
}
