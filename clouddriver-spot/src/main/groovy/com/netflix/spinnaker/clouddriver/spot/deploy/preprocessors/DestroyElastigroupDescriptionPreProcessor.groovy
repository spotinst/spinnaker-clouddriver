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

package com.netflix.spinnaker.clouddriver.spot.deploy.preprocessors

import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperationDescriptionPreProcessor
import com.netflix.spinnaker.clouddriver.spot.deploy.description.DestroyElastigroupDescription
import org.springframework.stereotype.Component

@Component
class DestroyElastigroupDescriptionPreProcessor implements AtomicOperationDescriptionPreProcessor {
  @Override
  boolean supports(Class descriptionClass) {
    return descriptionClass == DestroyElastigroupDescription

  }

  @Override
  Map process(Map description) {
    description.with {
      if (!elastigroups) {
        elastigroups = [[serverGroupName: serverGroupName ?: elastigroupName, region: region, elastigroupId: elastigroupId]]
      }

      // Only `elastigroups` should be propagated internally now.
      description.remove("elastigroupName")
      description.remove("serverGroupName")
      description.remove("regions")
      description.remove("region")
      description.remove("elastigroupId")

      return description
    }
  }
}
