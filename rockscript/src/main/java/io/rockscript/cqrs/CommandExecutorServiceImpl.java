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

package io.rockscript.cqrs;

import io.rockscript.engine.Configuration;

public class CommandExecutorServiceImpl implements CommandExecutorService {

  protected Configuration configuration;

  public CommandExecutorServiceImpl(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public <R extends Response> R execute(Command<R> command) {
    Command<R> commandImpl = (Command<R>) command;
    commandImpl.setConfiguration(configuration);
    return commandImpl.execute();
  }

  public Configuration getConfiguration() {
    return configuration;
  }
}
