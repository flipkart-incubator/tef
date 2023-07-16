/*
 * Copyright [2023] [The Original Author]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 
 * Date: 09/07/20
 * Time: 3:02 PM
 */
package flipkart.tef.execution;

import flipkart.tef.FlowExecutionListener;
import flipkart.tef.bizlogics.IBizlogic;

import java.util.ArrayList;
import java.util.List;

public class MyFlowExecutionListener implements FlowExecutionListener {

    List<ExecutionStep> executionOrder = new ArrayList<>();

    @Override
    public void pre(IBizlogic bizlogic) {
        executionOrder.add(new ExecutionStep(bizlogic.getClass(), ExecutionStage.PRE));
    }

    @Override
    public void post(IBizlogic bizlogic) {
        executionOrder.add(new ExecutionStep(bizlogic.getClass(), ExecutionStage.POST));
    }

    public List<ExecutionStep> getExecutionOrder() {
        return executionOrder;
    }
}
