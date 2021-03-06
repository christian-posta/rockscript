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

package io.rockscript.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.rockscript.activity.ImportResolver;
import io.rockscript.engine.impl.*;
import io.rockscript.engine.job.JobService;
import io.rockscript.http.GsonCodec;
import io.rockscript.http.Http;
import io.rockscript.cqrs.CommandExecutorService;
import io.rockscript.cqrs.CommandExecutorServiceImpl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;

import static io.rockscript.engine.impl.Event.createEventJsonTypeAdapterFactory;

public abstract class Configuration {

  protected IdGenerator scriptIdGenerator;
  protected IdGenerator scriptExecutionIdGenerator;
  protected IdGenerator jobIdGenerator;

  protected EventStore eventStore;
  protected ScriptStore scriptStore;
  protected EventListener eventListener;
  protected Engine engine;
  protected ImportResolver importResolver;
  protected Executor executor;
  protected Gson gson;
  protected Http http;
  protected JobService jobService;
  protected Map<String,Object> objects = new HashMap<>();

  public Configuration() {
    this.eventStore = new EventStore(this);
    this.scriptStore = new ScriptStore(this);
    this.eventListener = new EventLogger(this, eventStore);
    this.jobIdGenerator = new TestIdGenerator(this, "j");
    this.scriptIdGenerator = new TestIdGenerator(this, "s");
    this.scriptExecutionIdGenerator = new TestIdGenerator(this, "se");
    this.engine = new LocalEngine(this);
    this.importResolver = new ImportResolver(this);
    this.jobService = new JobService(this);
  }

  public CommandExecutorService build() {
    if (gson==null) {
      gson = createDefaultGson();
    }
    this.http = new Http(new GsonCodec(gson));
    throwIfNotProperlyConfigured();

    ServiceLoader<EngineModule> engineModules = ServiceLoader.load(EngineModule.class);
    for (EngineModule engineModule: engineModules) {
      engineModule.configured(this);
    }

    return new CommandExecutorServiceImpl(this);
  }

  private Gson createDefaultGson() {
    return new GsonBuilder()
      .registerTypeAdapterFactory(createEventJsonTypeAdapterFactory())
      .disableHtmlEscaping()
      .setPrettyPrinting()
      .create();
  }

  public void throwIfNotProperlyConfigured() {
    for (Field field: getClass().getDeclaredFields()) {
      Object value = null;
      try {
        field.setAccessible(true);
        value = field.get(this);
      } catch (IllegalAccessException e) {
        throw new EngineException(e);
      }
      EngineException.throwIfNull(value, "ServiceLocator field '%s' is null", field.getName());
    }
  }

  public Configuration object(String key, Object value) {
    objects.put(key, value);
    return this;
  }

  public Configuration object(Class<?> key, Object value) {
    if (value!=null && key!=Object.class) {
      objects.put(key.getName(), value);
      for (Class<?> interfaceClass: key.getInterfaces()) {
        object(interfaceClass, value);
      }
      object(key.getSuperclass(), value);
    }
    return this;
  }

  public Configuration object(Object value) {
    if (value!=null) {
      object(value.getClass(), value);
    }
    return this;
  }

  public <T> T getObject(String key) {
    return (T) objects.get(key);
  }

  public <T> T getObject(Class<T> clazz) {
    return (T) objects.get(clazz.getName());
  }

  public Configuration gson(Gson gson) {
    this.gson = gson;
    return this;
  }

  public IdGenerator getScriptIdGenerator() {
    return scriptIdGenerator;
  }

  public IdGenerator getScriptExecutionIdGenerator() {
    return scriptExecutionIdGenerator;
  }

  public IdGenerator getJobIdGenerator() {
    return jobIdGenerator;
  }

  public EventStore getEventStore() {
    return eventStore;
  }

  public ScriptStore getScriptStore() {
    return scriptStore;
  }

  public EventListener getEventListener() {
    return eventListener;
  }

  public Engine getEngine() {
    return engine;
  }

  public ImportResolver getImportResolver() {
    return importResolver;
  }

  public Gson getGson() {
    return gson;
  }

  public Executor getExecutor() {
    return executor;
  }

  public Http getHttp() {
    return http;
  }

  public JobService getJobService() {
    return jobService;
  }
}
