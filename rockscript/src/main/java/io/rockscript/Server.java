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
package io.rockscript;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.rockscript.engine.*;
import io.rockscript.gson.PolymorphicTypeAdapterFactory;
import io.rockscript.netty.router.AsyncHttpServer;
import io.rockscript.netty.router.AsyncHttpServerConfiguration;
import io.rockscript.netty.router.JsonHandlerGson;
import io.rockscript.server.handlers.CommandHandler;
import io.rockscript.server.handlers.EventsHandler;
import io.rockscript.server.handlers.PingHandler;
import io.rockscript.server.rest.ScriptsPostHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.BindException;

import static io.rockscript.engine.impl.Event.createEventJsonTypeAdapterFactory;

public class Server extends Rock {

  static Logger log = LoggerFactory.getLogger(Server.class);

  AsyncHttpServer asyncHttpServer;

  boolean parse(String[] args) {
    return true;
  }

  @Override
  void execute() {
    configure();
    startup();
  }

  @Override
  void showHelp() {
    log("TODO add options and document them");
//    HelpFormatter formatter = new HelpFormatter();
//    formatter.printHelp( "rock server [server options]", createCommandLineOptions());
  }

  private void configure() {
    DevConfiguration serviceConfiguration = new DevConfiguration();

    ServerConfiguration serverConfiguration = new ServerConfiguration(serviceConfiguration);
    Gson commonGson = new GsonBuilder()
      .registerTypeAdapterFactory(createCommandsTypeAdapterFactory())
      .registerTypeAdapterFactory(createEventJsonTypeAdapterFactory())
      .create();

    serviceConfiguration.gson(commonGson);
    ScriptService scriptService = serviceConfiguration.build();

    AsyncHttpServerConfiguration asyncHttpServerConfiguration = serverConfiguration
      .getAsyncHttpServerConfiguration()
      .scan(CommandHandler.class)
      .scan(EventsHandler.class)
      .scan(PingHandler.class)
      .scan(ScriptsPostHandler.class)
      .jsonHandler(new JsonHandlerGson(commonGson))
      .context(ScriptService.class, scriptService)
      .context(Configuration.class, serviceConfiguration);

    this.asyncHttpServer = new AsyncHttpServer(asyncHttpServerConfiguration);
  }

  public static PolymorphicTypeAdapterFactory createCommandsTypeAdapterFactory() {
    return new PolymorphicTypeAdapterFactory()
      .typeName(new TypeToken<Command>(){}, "command")
      .typeName(new TypeToken<DeployScriptCommand>(){}, "deployScript")
      .typeName(new TypeToken<StartScriptExecutionCommand>(){}, "startScript")
      .typeName(new TypeToken<EndActivityCommand>(){}, "endActivity")
      .typeName(new TypeToken<RunTestsCommand>(){}, "runTests");
  }

  public void startup() {
    log(" ____            _     ____            _       _    ");
    log("|  _ \\ ___   ___| | __/ ___|  ___ _ __(_)_ __ | |_  ");
    log("| |_) / _ \\ / __| |/ /\\___ \\ / __| '__| | '_ \\| __| ");
    log("|  _ < (_) | (__|   <  ___) | (__| |  | | |_) | |_  ");
    log("|_| \\_\\___/ \\___|_|\\_\\|____/ \\___|_|  |_| .__/ \\__| ");
    log("                                        |_|         ");
    try {
      asyncHttpServer.startup();
      log("Server started on port "+asyncHttpServer.getPort());

    } catch (Throwable t) {
      if ("Address already in use".equals(t.getMessage())
        && (t instanceof BindException)) {
        log("ERROR: Port "+asyncHttpServer.getPort()+" is already taken");
      } else {
        log("ERROR: RockScript server could not be started: "+t.getMessage());
      }
      asyncHttpServer.shutdown();
    }
  }

  public void shutdown() {
    asyncHttpServer.shutdown();
  }

  public void waitForShutdown() {
    asyncHttpServer.waitForShutdown();
  }
}