package io.rockscript.cqrs.commands;

import io.rockscript.engine.ScriptExecution;
import io.rockscript.cqrs.Response;

import java.util.List;

public class RecoverCrashedScriptExecutionsResponse implements Response {

  public List<ScriptExecution> scriptExecutions;

  public RecoverCrashedScriptExecutionsResponse() {
  }

  public RecoverCrashedScriptExecutionsResponse(List<ScriptExecution> scriptExecutions) {
    this.scriptExecutions = scriptExecutions;
  }

  @Override
  public int getStatus() {
    return 200;
  }

  public List<ScriptExecution> getScriptExecutions() {
    return scriptExecutions;
  }
}
