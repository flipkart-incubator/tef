/*
 *Copyright [2024] [The Original Author]
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
 * Date: 14/07/20
 * Time: 9:12 AM
 */
package flipkart.tef.execution;


import flipkart.tef.TestTefContext;
import flipkart.tef.annotations.DependsOn;
import flipkart.tef.annotations.InjectData;
import flipkart.tef.bizlogics.DataAdapterBizlogic;
import flipkart.tef.bizlogics.DataAdapterResult;
import flipkart.tef.bizlogics.IBizlogic;
import flipkart.tef.bizlogics.TefContext;
import flipkart.tef.exception.DataDependencyException;
import flipkart.tef.exception.TefExecutionException;
import flipkart.tef.flow.SimpleFlow;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FlowExecutorMutationTest {

    public static final String TEST_TENANT = "TestTenant";
    private FlowBuilder flowBuilder;

    // static so that inner classes can access
    private static List<DataA> dataToReturn = null;
    private static int dataIndex;

    @BeforeClass
    public static void setup() {

    }

    @Before
    public void setUp() {
        flowBuilder = new FlowBuilder();
        dataToReturn = new ArrayList<>();
        dataToReturn.add(new DataA(1));
        dataToReturn.add(new DataA(2));
        dataIndex = 0;
    }

    @Ignore
    @Test
    public void testMutation() throws IllegalAccessException, DataDependencyException, InstantiationException, TefExecutionException {

        FlowExecutorTest.SampleDataAdapter.ADAPTED = new FlowExecutorTest.SampleData();

        /**
         * Validator1 and Validator2 depends on DataA
         * DataADataAdapter's DataB dependency is resolved via implicit binding
         * So the order of execution is
         * 1. DataADataAdapter
         * 2. Followed by Validator1 (since DataA is now available)
         * 3. Followed by DataBDataAdapter since Validator2 defines a control dependency on it.
         * 4. Followed by Validator2 since it depends on DataA and DataBDataAdapter (via control dependency)
         */
        flowBuilder.add(Validator1.class);
        flowBuilder.add(Validator2.class);
        flowBuilder.add(DataADataAdapter.class);
        flowBuilder.add(DataBDataAdapter.class);

        flowBuilder.withImplicitBindings(DataB.class);
        assertEquals(4, flowBuilder.getBizlogics().size());

        SimpleFlow flow = flowBuilder.build();

        List<Class<? extends IBizlogic>> bizlogics = new ArrayList<>(flow.getBizlogics());
        assertEquals(4, bizlogics.size());
        assertEquals(DataADataAdapter.class, bizlogics.get(0));
        assertEquals(Validator1.class, bizlogics.get(1));
        assertEquals(DataBDataAdapter.class, bizlogics.get(2));
        assertEquals(Validator2.class, bizlogics.get(3));

        DataContext dataContext = new DataContext();
        dataContext.put(new DataAdapterResult(new DataB()));
        FlowExecutor executor = new FlowExecutor(flow, dataContext, new TestTefContext());

        MyFlowExecutionListener listener = new MyFlowExecutionListener();
        executor.addListener(listener);
        executor.execute();

        /**
         * The expected execution order is as follows
         * 1. DataAAdapter executes first and its dependency on DataB is resolved via implicit binding
         * 2. This is followed by Validator1's execution since DataA is available
         * 3. This is followed by DataBDataAdapter's execution, where DataA is injected from #1
         * 4. In #3, a new instance of DataB is emitted, and since DataADataAdapter's injection of DataB is listening for mutations
         *      this changed DataB causes a re-trigger of DataADataAdapter for whoever
         * 5. Validator2 executes with a new value of DataA
         */

        List<ExecutionStep> executionOrder = listener.getExecutionOrder();

        Integer idx = 0;

        // DataADataAdapter starts execution (both pre and post)
        idx = assertExecutionOrder(executionOrder, idx, DataADataAdapter.class);

        // Validator1 starts execution
        assertEquals(ExecutionStage.PRE, executionOrder.get(idx).stage);
        assertEquals(Validator1.class, executionOrder.get(idx++).bizlogic);

        // Validator1 injects DataA and causes DataADataAdapter's pre/post to get executed (returns data from cache)
        idx = assertExecutionOrder(executionOrder, idx, DataADataAdapter.class);

        // Validator1 completes execution
        assertEquals(ExecutionStage.POST, executionOrder.get(idx).stage);
        assertEquals(Validator1.class, executionOrder.get(idx++).bizlogic);

        // DataBDataAdapter begins execution
        assertEquals(ExecutionStage.PRE, executionOrder.get(idx).stage);
        assertEquals(DataBDataAdapter.class, executionOrder.get(idx++).bizlogic);

        // DataBDataAdapter injects DataA and causes DataADataAdapter's pre/post to get executed (invalidates cache and triggers adapt method)
        idx = assertExecutionOrder(executionOrder, idx, DataADataAdapter.class);

        // DataBDataAdapter completes execution
        assertEquals(ExecutionStage.POST, executionOrder.get(idx).stage);
        assertEquals(DataBDataAdapter.class, executionOrder.get(idx++).bizlogic);

        // Validator2 starts execution
        assertEquals(ExecutionStage.PRE, executionOrder.get(idx).stage);
        assertEquals(Validator2.class, executionOrder.get(idx++).bizlogic);

        // Validator2 injects DataA and causes DataADataAdapter's pre/post to get executed (returns data from cache)
        idx = assertExecutionOrder(executionOrder, idx, DataADataAdapter.class);

        // Validator2 completes execution
        assertEquals(ExecutionStage.POST, executionOrder.get(idx).stage);
        assertEquals(Validator2.class, executionOrder.get(idx++).bizlogic);
    }

    private Integer assertExecutionOrder(List<ExecutionStep> executionOrder, Integer idx, Class executed) {
        assertEquals(ExecutionStage.PRE, executionOrder.get(idx).stage);
        assertEquals(executed, executionOrder.get(idx++).bizlogic);
        assertEquals(ExecutionStage.POST, executionOrder.get(idx).stage);
        assertEquals(executed, executionOrder.get(idx++).bizlogic);
        return idx;
    }

    public static class DataA {
        int data = 0;

        public DataA(int data) {
            this.data = data;
        }
    }

    public static class DataB {

    }

    public static class DataADataAdapter extends DataAdapterBizlogic<DataA> {

        // Whenever DataB changes, this adapter will be force-invoked to get a fresh value of DataA
        // At the first invocation, DataB is present in the DataContext
        @InjectData(mutable = true)
        private DataB dataB;

        @Override
        public DataA adapt(TefContext tefContext) {
            return dataToReturn.get(dataIndex++);
        }
    }

    public static class DataBDataAdapter extends DataAdapterBizlogic<DataB> {

        @InjectData
        private DataA dataA;

        // This bizlogic modifies the DataB (that was present in DataContext)
        // hence triggering the invalidation of DataA.
        @Override
        public DataB adapt(TefContext tefContext) {
            assertEquals(1, dataA.data);
            return new DataB();
        }
    }

    public static class Validator1 implements IBizlogic {

        // When DataA is injected here, it is right after the first invocation
        @InjectData
        private DataA dataA;

        @Override
        public void execute(TefContext tefContext) {
            assertEquals(1, dataA.data);
        }
    }

    /**
     * In the ideal world, Data Dependency should come via Data injections, and not via control dependency.
     * The code here is just to ensure a specific order.
     */
    @DependsOn(DataBDataAdapter.class)
    public static class Validator2 implements IBizlogic {

        // When DataA is injected here, DataAdapter for DataA is re-triggered.
        @InjectData
        private DataA dataA;

        @Override
        public void execute(TefContext tefContext) {
            assertEquals(2, dataA.data);
        }
    }
}
