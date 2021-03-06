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
 *
 */
package io.rockscript;

import io.rockscript.cqrs.commands.DeployScriptCommand;
import io.rockscript.cqrs.commands.EngineDeployScriptResponse;
import io.rockscript.engine.ParseError;
import io.rockscript.test.AbstractServerTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DeployTest extends AbstractServerTest {

  protected static Logger log = LoggerFactory.getLogger(DeployTest.class);

  @Test
  public void testDeployOk() {
    EngineDeployScriptResponse deployScriptResponse = newPost("command")
      .bodyObject(new DeployScriptCommand()
        .scriptText("var a=0;")
        .scriptName("Test script"))
      .execute()
      .assertStatusOk()
      .getBodyAs(EngineDeployScriptResponse.class);

    assertNotNull(deployScriptResponse.getId());
    assertEquals((Integer) 1, deployScriptResponse.getVersion());
    assertEquals("Test script", deployScriptResponse.getName());
    assertNull(deployScriptResponse.getErrors());
  }

  @Test
  public void testDeploySyntaxError() {
    EngineDeployScriptResponse deployScriptResponse = newPost("command")
      .bodyObject(new DeployScriptCommand()
        .scriptText("\n\ninvalid script"))
      .execute()
      .assertStatusBadRequest()
      .getBodyAs(EngineDeployScriptResponse.class);

    assertEquals("Unnamed script", deployScriptResponse.getName());

    List<ParseError> errors = deployScriptResponse.getErrors();
    assertEquals(1, errors.size());
    ParseError error = errors.get(0);
    assertEquals(3, error.getLine());
    assertEquals(8, error.getColumn());
    assertEquals("no viable alternative at input 'script'", error.getMessage());
  }

  @Test
  public void testDeployParseError() {
    EngineDeployScriptResponse deployScriptResponse = newPost("command")
      .bodyObject(new DeployScriptCommand()
        .scriptText("\n\nvar a = new Object();"))
      .execute()
      .assertStatusBadRequest()
      .getBodyAs(EngineDeployScriptResponse.class);

    assertEquals("Unnamed script", deployScriptResponse.getName());

    List<ParseError> errors = deployScriptResponse.getErrors();
    assertEquals(1, errors.size());
    ParseError error = errors.get(0);
    assertEquals(3, error.getLine());
    assertEquals(8, error.getColumn());
    assertEquals("Unsupported singleExpression: newObject() NewExpressionContext", error.getMessage());
  }
}
