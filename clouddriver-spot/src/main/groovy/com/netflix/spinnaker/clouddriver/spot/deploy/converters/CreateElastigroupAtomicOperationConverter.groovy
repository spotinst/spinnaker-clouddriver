/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.spot.deploy.converters

import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperation
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperations
import com.netflix.spinnaker.clouddriver.security.AbstractAtomicOperationsCredentialsSupport
import com.netflix.spinnaker.clouddriver.spot.SpotOperation
import com.netflix.spinnaker.clouddriver.spot.deploy.description.CreateElastigroupDescription
import com.netflix.spinnaker.clouddriver.spot.deploy.ops.CreateElastigroupAtomicOperation

import groovy.json.JsonSlurper
import org.springframework.stereotype.Component
@SpotOperation(AtomicOperations.DEPLOY_ELASTIGROUP)
@Component("deployElastigroup")
class CreateElastigroupAtomicOperationConverter extends AbstractAtomicOperationsCredentialsSupport {
  @Override
  AtomicOperation convertOperation(Map input) {
    new CreateElastigroupAtomicOperation(convertDescription(input))
  }

  @Override
  CreateElastigroupDescription convertDescription(Map input) {
    def converted = objectMapper.convertValue(input, CreateElastigroupDescription)
    converted.credentials = getCredentialsObject(input.credentials as String)
    def jsonSlurper = new JsonSlurper()
    def elastigroupMap = jsonSlurper.parseText(input.payload)
    converted.elastigroup = elastigroupMap["group"]
    converted
  }
}
