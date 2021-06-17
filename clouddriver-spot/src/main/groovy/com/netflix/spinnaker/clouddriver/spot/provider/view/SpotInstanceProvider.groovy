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

package com.netflix.spinnaker.clouddriver.spot.provider.view

import com.netflix.spinnaker.cats.cache.Cache
import com.netflix.spinnaker.cats.cache.CacheData
import com.netflix.spinnaker.clouddriver.model.InstanceProvider
import com.netflix.spinnaker.clouddriver.spot.SpotCloudProvider
import com.netflix.spinnaker.clouddriver.spot.cache.Keys
import com.netflix.spinnaker.clouddriver.spot.model.SpotInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import static com.netflix.spinnaker.clouddriver.core.provider.agent.Namespace.INSTANCES

@Component
class SpotInstanceProvider implements InstanceProvider<SpotInstance, String> {
  private Cache cacheView;

  @Autowired
  SpotInstanceProvider(Cache cacheView) {
    this.cacheView = cacheView;
  }


  @Override
  public String getCloudProvider() {
    return SpotCloudProvider.ID;
  }

  @Override
  public SpotInstance getInstance(String account, String region, String id) {
    SpotInstance retVal = new SpotInstance();
    String instanceKey = Keys.getInstanceKey(id, account, region);
    CacheData cacheData = cacheView.get(INSTANCES.ns, instanceKey);

    if(cacheData != null) {
      retVal = new SpotInstance(cacheData.getAttributes())
    }

    return retVal
  }

  @Override
  public String getConsoleOutput(String account, String region, String id) {
    return null;
  }
}
