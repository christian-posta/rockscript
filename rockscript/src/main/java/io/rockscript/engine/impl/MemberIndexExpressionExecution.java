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

package io.rockscript.engine.impl;

import java.util.ArrayList;
import java.util.List;

import static io.rockscript.engine.impl.MemberDotExpressionExecution.getFieldValue;

public class MemberIndexExpressionExecution extends Execution<MemberIndexExpression> {

  public MemberIndexExpressionExecution(MemberIndexExpression element, Execution parent) {
    super(parent.createInternalExecutionId(), element, parent);
  }

  @Override
  public void start() {
    startChild(element.getBaseExpression());
  }

  @Override
  public void childEnded(Execution child) {
    startNextParameter();
  }

  private void startNextParameter() {
    int indexIndex = children.size()-1; // -1 because the first one is the function expression
    List<SingleExpression> indexExpressions = element.getExpressionSequence();
    if (indexIndex < indexExpressions.size()) {
      ScriptElement indexExpression = indexExpressions.get(indexIndex);
      startChild(indexExpression);
    } else {
      Object result = children.get(0).getResult();
      List<Object> indices = collectResultsFromChildren()
        .subList(1, children.size());
      for (int i=0; i<indices.size() && result!=null; i++) {
        Object index = indices.get(i);
        result = getFieldValue(result, index);
      }
      setResult(result);
    }
  }
}
