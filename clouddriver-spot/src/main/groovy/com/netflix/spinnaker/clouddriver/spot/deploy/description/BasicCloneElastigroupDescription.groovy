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

package com.netflix.spinnaker.clouddriver.spot.deploy.description


import com.netflix.spinnaker.clouddriver.deploy.DeployDescription
import com.netflix.spinnaker.clouddriver.orchestration.events.OperationEvent
import com.netflix.spinnaker.clouddriver.security.resources.ApplicationNameable
import groovy.transform.AutoClone
import groovy.transform.Canonical

@AutoClone
@Canonical
class BasicCloneElastigroupDescription extends AbstractSpotCredentialsDescription implements DeployDescription, ApplicationNameable {
  String application
  String imageId
  Collection<OperationEvent> events = []
  Source source = new Source()


  @Override
  Collection<String> getApplications() {
    return [application]
  }

  @Override
  String toString() {
    return "BasicCloneElastigroupDescription{" +
      "application='" + application + '\'' +
      ", imageId='" + imageId + '\'' +
      ", events=" + events +
      ", source=" + source +
      '}';
  }


  @Canonical
  static class Source {
    String account
    String region
    String asgName
    Boolean useSourceCapacity
  }
}
