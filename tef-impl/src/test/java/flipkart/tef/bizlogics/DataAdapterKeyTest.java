/*
 * Copyright [2024] [The Original Author]
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

package flipkart.tef.bizlogics;


import flipkart.tef.TestTefContext;
import flipkart.tef.annotations.EmitData;
import flipkart.tef.annotations.InjectData;
import flipkart.tef.exception.DataDependencyException;
import flipkart.tef.exception.TefExecutionException;
import flipkart.tef.execution.DataContext;
import flipkart.tef.execution.FlowExecutor;
import flipkart.tef.execution.FluentCapabilityBuilder;
import flipkart.tef.flow.SimpleFlow;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for DataAdapterResultKey
 * 
 * Date: 19/01/21
 */
public class DataAdapterKeyTest {



    @BeforeClass
    public static void setup() {

    }

    @Test
    public void testSimpleNamedInjection() throws IllegalAccessException, DataDependencyException, InstantiationException, TefExecutionException {

        FluentCapabilityBuilder capabilityBuilder = new FluentCapabilityBuilder();
        SimpleFlow flow = capabilityBuilder.withAdapter(DataAdapter1.class)
                   .withBizlogic(SampleBizlogic.class).dataflow();

        FlowExecutor flowExecutor = new FlowExecutor(flow, new DataContext(), new TestTefContext());
        flowExecutor.execute();

    }

    @Test
    public void testMultiNamedInjection() throws IllegalAccessException, DataDependencyException, InstantiationException, TefExecutionException {
        FluentCapabilityBuilder capabilityBuilder = new FluentCapabilityBuilder();
        SimpleFlow flow = capabilityBuilder.withAdapter(DataAdapter1.class)
                .withAdapter(DataAdapter2.class)
                .withBizlogic(SampleBizlogic.class).dataflow();

        FlowExecutor flowExecutor = new FlowExecutor(flow, new DataContext(), new TestTefContext());
        flowExecutor.execute();
    }

    public static class SampleData {
        public int value;

        public SampleData(int value) {
            this.value = value;
        }
    }

    @EmitData(name = "1")
    public static class DataAdapter1 extends DataAdapterBizlogic<SampleData> {

        @Override
        public SampleData adapt(TefContext tefContext) {
            return new SampleData(1);
        }
    }

    @EmitData(name = "2")
    public static class DataAdapter2 extends DataAdapterBizlogic<SampleData> {

        @Override
        public SampleData adapt(TefContext tefContext) {
            return new SampleData(2);
        }
    }

    public static class SampleBizlogic implements IBizlogic {

        @InjectData(name = "1")
        private SampleData sampleData1;

        @Override
        public void execute(TefContext tefContext) {
            assertEquals(sampleData1.value, 1);
        }
    }
}
