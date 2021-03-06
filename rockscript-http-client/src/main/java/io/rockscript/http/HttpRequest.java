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

package io.rockscript.http;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.rockscript.http.Http.Methods.*;

/** AsyncHttpRequest builder */
public class HttpRequest {

  static final String NEWLINE = System.getProperty("line.separator");

  private static final EntityHandler DEFAULT_ENTITY_HANDLER = new StringEntityHandler();

  /** transient because this field should not be serialized by gson */
  transient Http http;
  /** transient because this field should not be serialized by gson */
  transient HttpRequestBase apacheRequest;
  /** transient because this field should not be serialized by gson */
  transient EntityHandler entityHandler = DEFAULT_ENTITY_HANDLER;

  protected String method;
  protected String url;
  protected Map<String,List<String>> headers;
  protected Object body;

  protected HttpRequest(Http http, String method, String url) {
    this.http = http;
    this.method = method;
    this.url = url;
  }

  /** Executes the request and returns the H */
  public HttpResponse execute() {
    try {
      if (GET.equals(method)) {
        this.apacheRequest = new HttpGet(url);
      } else if (POST.equals(method)) {
        this.apacheRequest = new HttpPost(url);
      } else if (PUT.equals(method)) {
        this.apacheRequest = new HttpPost(url);
      } else if (DELETE.equals(method)) {
        this.apacheRequest = new HttpPost(url);
      } else {
        throw new RuntimeException("Invalid HTTP method "+method+" for request "+url);
      }

      if (headers!=null) {
        for (String headerName: headers.keySet()) {
          for (String headerValue: headers.get(headerName)) {
            apacheRequest.addHeader(headerName, headerValue);
          }
        }
      }

      if (body!=null) {
        if (body instanceof String) {
          String bodyString = (String) body;
          HttpEntity entity = new ByteArrayEntity(bodyString.getBytes("UTF-8"));
          ((HttpEntityEnclosingRequestBase)apacheRequest).setEntity(entity);
        } else {
          throw new RuntimeException("Unsupported body type "+body.getClass().getName());
        }
      }

      return createHttpResponse();

    } catch (IOException e) {
      throw new RuntimeException("Couldn't execute request "+url+": "+e.getMessage(), e);
    }
  }

  protected HttpResponse createHttpResponse() throws IOException {
    return new HttpResponse(this);
  }

  /** Logs the request in human readable form to the {@link Http} Slf4j logger */

  @Override
  public String toString() {
    return toString(null);
  }

  public String toString(String prefix) {
    return toString(prefix, null);
  }

  public String toString(String prefix, Integer maxBodyLength) {
    StringBuilder text = new StringBuilder();
    if (prefix==null) {
      prefix = "";
    }
    text.append(prefix);
    text.append("> ");
    text.append(method);
    text.append(" ");
    text.append(url);
    if (headers!=null) {
      for (String headerName: headers.keySet()) {
        List<String> headerListValue = headers.get(headerName);
        String headerValue = headerListValue
          .stream()
          .collect(Collectors.joining("; "));
        text.append(NEWLINE);
        text.append(prefix);
        text.append("  ");
        text.append(headerName);
        text.append(": ");
        text.append(headerValue);
      }
    }
    if (body!=null && !"".equals(body)) {
      text.append(NEWLINE);
      text.append(prefix);
      text.append("  ");

      String bodyString = body.toString();
      if (maxBodyLength!=null && bodyString!=null && bodyString.length()>maxBodyLength) {
        bodyString = bodyString.substring(0, maxBodyLength)+"...";
      }
      text.append(bodyString);
    }
    return text.toString();
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  /** adds a query parameter if the valueUnencoded is not null,
   * both parameterName and valueUnencoded will be encoded using URLEncoder */
  public HttpRequest queryParameterNotNull(String parameterName, String value) {
    if (parameterName!=null && value!=null) {
      if (url.contains("?")) {
        url += "&";
      } else {
        url += "?";
      }
      try {
        url += URLEncoder.encode(parameterName, "UTF-8")+"="+URLEncoder.encode(value, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException("Couldn't set parameter");
      }
    }
    return this;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, List<String>> headers) {
    this.headers = headers;
  }

  public HttpRequest header(String name, String value) {
    if (headers==null) {
      headers = new HashMap<>();
    }
    List<String> values = headers.get(name);
    if (values==null) {
      values = new ArrayList<>();
      headers.put(name, values);
    }
    values.add(value);
    return this;
  }

  public HttpRequest headerContentType(String contentType) {
    header(Http.Headers.CONTENT_TYPE, contentType);
    return this;
  }

  public HttpRequest headerContentTypeApplicationJson() {
    headerContentType(Http.ContentTypes.APPLICATION_JSON);
    return this;
  }

  /** Sets a String as the body for the request */
  public void setBody(String body) {
    this.body = body;
  }
  /** Sets a String as the body for the request */
  public HttpRequest body(String body) {
    setBody(body);
    return this;
  }

  /** Uses the {@link Http} {@link Codec} to transform
   * the given bodyObject to a string. */
  public HttpRequest bodyObject(Object bodyObject) {
    setBodyObject(bodyObject);
    return this;
  }

  /** See {@link #bodyObject(Object)} */
  public void setBodyObject(Object bodyObject) {
    this.body = http.getCodec().serialize(bodyObject);
  }

  public Http getHttp() {
    return http;
  }

  public void setHttp(Http http) {
    this.http = http;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public void setBody(Object body) {
    this.body = body;
  }

  public EntityHandler getEntityHandler() {
    return this.entityHandler;
  }

  /** See {@link EntityHandler} */
  public void setEntityHandler(EntityHandler entityHandler) {
    this.entityHandler = entityHandler;
  }

  /** Fluent setter to overwrite the default {@link StringEntityHandler}.
   * Check out {@link EntityHandler} for more info. */
  public HttpRequest entityHandler(EntityHandler entityHandler) {
    this.entityHandler = entityHandler;
    return this;
  }

  public HttpRequestBase getApacheRequest() {
    return apacheRequest;
  }
}
