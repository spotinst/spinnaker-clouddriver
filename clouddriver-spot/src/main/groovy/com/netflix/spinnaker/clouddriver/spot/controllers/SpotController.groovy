/*
 * Copyright 2021 Netflix, Inc.
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

package com.netflix.spinnaker.clouddriver.spot.controllers

import com.netflix.spinnaker.clouddriver.security.AccountCredentialsRepository
import com.netflix.spinnaker.clouddriver.spot.security.SpotCredentialsInitializer
import com.spotinst.sdkjava.enums.EventsLogsSeverityEnum
import com.spotinst.sdkjava.model.EventLog
import com.spotinst.sdkjava.model.GetEventsLogsRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Configuration
@EnableConfigurationProperties
@Import([SpotCredentialsInitializer])
@RestController
@RequestMapping("/applications/{application}/clusters/{account}/{clusterName}/spot/serverGroups/{serverGroupName}")
class SpotController {

  @Autowired
  AccountCredentialsRepository accountCredentialsRepository

  final static LIMIT = "1000"

  @RequestMapping(value = "/elastilogs", method = RequestMethod.GET)
  ResponseEntity getElastilogs(@PathVariable String account, @PathVariable String serverGroupName, @RequestParam(value = "elastigroupId", required = true) String elastigroupId, @RequestParam(value = "fromDate", required = true) String fromDate, @RequestParam(value = "toDate", required = true) String toDate, @RequestParam(value = "severity", required = true) String severity) {

    def accountCredentialsProvider = accountCredentialsRepository.getOne(account)

    def elastigroupClient = accountCredentialsProvider.elastigroupClient

    def severityEnum = EventsLogsSeverityEnum.fromName(severity)

    if(Objects.equals(severityEnum.ALL)){
      severityEnum = null;
    }

    def getAllElastigroupsRequest = GetEventsLogsRequest.Builder.get().setElastigroupId(elastigroupId).setFromDate(fromDate).setToDate(toDate).setSeverity(severityEnum).setLimit(LIMIT).build()
    List<EventLog> eventsLogs = elastigroupClient.getEventsLogs(getAllElastigroupsRequest)

    return new ResponseEntity(eventsLogs, HttpStatus.OK)
  }
}


