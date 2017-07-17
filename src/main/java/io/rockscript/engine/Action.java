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

import java.util.List;

public interface Action {

  /**
   * TODO describe how argumentsExpressionExecution.id is the current execution position within the script execution.
   *      This execution position  has to be provided in case the invocation is asynchronous when calling back
   *      the completion of this function with {@link ScriptExecution#endFunctionInvocationExecution(String, Object)}
   */
  ActionResponse invoke(ArgumentsExpressionExecution argumentsExpressionExecution, List<Object> args);

}
