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
package com.netflix.spinnaker.clouddriver.spot.service.serverGroup

import com.netflix.spinnaker.clouddriver.spot.model.SpotServerGroup
import com.netflix.spinnaker.clouddriver.spot.security.SpotAccountCredentials

interface SpotServerGroupService {

  List<SpotServerGroup> listAllServerGroups(SpotAccountCredentials creds)

//  public List<String> listServerGroupNamesByClusterName(SpotNamedAccountCredentials creds, String clusterName)
//
//  public SpotServerGroup getServerGroup(SpotNamedAccountCredentials creds, String application, String name)
//
//  public void createServerGroup(Task task, SpotServerGroup serverGroup)
//
//  public boolean destroyServerGroup(Task task, SpotNamedAccountCredentials creds, String serverGroupName)
//
//  public boolean resizeServerGroup(Task task, SpotNamedAccountCredentials creds, String serverGroupName, Integer targetSize)
//
//  public void disableServerGroup(Task task, SpotNamedAccountCredentials creds, String serverGroupName)
//
//  public void enableServerGroup(Task task, SpotNamedAccountCredentials creds, String serverGroupName)
//
//  public void updateServerGroup(SpotServerGroup sg)
}
