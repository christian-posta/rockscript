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

package io.rockscript.activity.http;

import io.rockscript.engine.impl.ContinuationReference;
import io.rockscript.engine.impl.Engine;
import io.rockscript.http.HttpRequest;
import io.rockscript.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpRequestRunnable implements Runnable {

  static Logger log = LoggerFactory.getLogger(HttpRequestRunnable.class);

  ContinuationReference continuationReference;
  HttpRequest request;
  Engine engine;

  public HttpRequestRunnable(ContinuationReference continuationReference, HttpRequest request, Engine engine) {
    this.continuationReference = continuationReference;
    this.request = request;
    this.engine = engine;
  }

  @Override
  public void run() {
    HttpResponse response = request
      .entityHandler((httpEntity,httpResponse)->{
        try {
          Object body = EntityUtils.toString(httpEntity, "UTF-8");
          if (httpResponse.isContentTypeApplicationJson()) {
            body = httpResponse
              .getRequest()
              .getHttp()
              .getCodec()
              .deserialize((String)body, Object.class);
          }
          return body;
        } catch (IOException e) {
          throw new RuntimeException("Couldn't ready body/entity from http request "+httpResponse.toString());
        }
      })
      .execute();
    engine.endActivity(continuationReference, response);
  }
}
