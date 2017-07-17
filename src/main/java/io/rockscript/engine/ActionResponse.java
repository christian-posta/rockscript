/*
 * Copyright ©2017, RockScript.io. All rights reserved.
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
package io.rockscript.engine;

public class ActionResponse {

  private boolean ended;
  private Object result;

  protected ActionResponse() {
  }

  public static ActionResponse endFunction() {
    return endFunction(null);
  }

  public static ActionResponse endFunction(Object result) {
    ActionResponse actionResponse = new ActionResponse();
    actionResponse.result = result;
    actionResponse.ended = true;
    return actionResponse;
  }

  public static ActionResponse waitForFunctionToCompleteAsync() {
    ActionResponse actionResponse = new ActionResponse();
    actionResponse.ended = false;
    return actionResponse;
  }

  public boolean isEnded() {
    return ended;
  }

  public Object getResult() {
    return result;
  }
}
