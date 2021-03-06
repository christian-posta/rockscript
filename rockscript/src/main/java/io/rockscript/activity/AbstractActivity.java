/*
 * Copyright (c) 2017, RockScript.io. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rockscript.activity;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractActivity implements Activity {

  String serviceName;
  String activityName;
  List<String> argNames;

  public AbstractActivity(String activityName, String... argNames) {
    this.activityName = activityName;
    this.argNames = argNames!=null ? Arrays.asList(argNames) : null;
  }

  @Override
  public String getActivityName() {
    return activityName;
  }

  @Override
  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  @Override
  public List<String> getArgNames() {
    return argNames;
  }

  @Override
  public String toString() {
    return serviceName+"/"+activityName;
  }
}
