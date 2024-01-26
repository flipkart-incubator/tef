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

package flipkart.tef.execution;

import flipkart.tef.TestTefContext;
import flipkart.tef.annotations.InjectData;
import flipkart.tef.bizlogics.BasicEnrichmentBizlogic;
import flipkart.tef.bizlogics.DataAdapterBizlogic;
import flipkart.tef.bizlogics.DataAdapterResult;
import flipkart.tef.bizlogics.IBizlogic;
import flipkart.tef.bizlogics.TefContext;
import flipkart.tef.capability.model.EnrichmentResultData;
import flipkart.tef.capability.model.MapBaseData;
import flipkart.tef.exception.TefExecutionException;
import flipkart.tef.flow.SimpleFlow;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class FlowExecutorTest {

    private FlowBuilder flowBuilder;

    @BeforeClass
    public static void setup() {

    }

    @Before
    public void setUp() {
        flowBuilder = new FlowBuilder();
    }

    @Test
    public void testBasicFlowExecution() throws IllegalAccessException, DataDependencyException, InstantiationException, TefExecutionException {
        Class<? extends IBizlogic> adapterClass = SampleDataAdapter.class;
        Class<? extends IBizlogic> enricherClass = SampleEnrichmentBizlogic.class;

        SampleDataAdapter.ADAPTED = new SampleData();

        flowBuilder.add(adapterClass);
        flowBuilder.add(enricherClass);

        assertEquals(2, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(enricherClass));
        assertTrue(flowBuilder.getBizlogics().contains(adapterClass));

        SimpleFlow flow = flowBuilder.build();
        DataContext dataContext = new DataContext();
        FlowExecutor executor = new FlowExecutor(flow, dataContext, new TestTefContext());

        MyFlowExecutionListener listener = new MyFlowExecutionListener();
        executor.addListener(listener);
        executor.execute();
        List<ExecutionStep> executionOrder = listener.getExecutionOrder();

        int idx = 0;

        idx = assertExecutionOrder(executionOrder, idx, adapterClass);

        assertEquals(executionOrder.get(idx).stage, ExecutionStage.PRE);
        assertEquals(executionOrder.get(idx++).bizlogic, enricherClass);

        idx = assertExecutionOrder(executionOrder, idx, adapterClass);

        assertEquals(executionOrder.get(idx).bizlogic, enricherClass);
        assertEquals(executionOrder.get(idx).stage, ExecutionStage.POST);
    }

    private Integer assertExecutionOrder(List<ExecutionStep> executionOrder, Integer idx, Class executed) {
        assertEquals(ExecutionStage.PRE, executionOrder.get(idx).stage);
        assertEquals(executed, executionOrder.get(idx++).bizlogic);

        assertEquals(ExecutionStage.POST, executionOrder.get(idx).stage);
        assertEquals(executed, executionOrder.get(idx++).bizlogic);
        return idx;
    }

    @Test
    public void testFlowExecutionWithNullData() throws IllegalAccessException, DataDependencyException, InstantiationException, TefExecutionException {
        Class<? extends IBizlogic> adapterClass = SampleDataAdapter.class;
        Class<? extends IBizlogic> enricherClass = SampleEnrichmentBizlogic.class;

        SampleDataAdapter.ADAPTED = null;

        flowBuilder.add(adapterClass);
        flowBuilder.add(enricherClass);

        assertEquals(2, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(enricherClass));
        assertTrue(flowBuilder.getBizlogics().contains(adapterClass));

        SimpleFlow flow = flowBuilder.build();
        List<Class<? extends IBizlogic>> order = flow.getBizlogics();
        assertEquals(adapterClass, order.get(0));
        assertEquals(enricherClass, order.get(1));

        FlowExecutor executor = new FlowExecutor(flow, new DataContext(), new TestTefContext());
        MyFlowExecutionListener listener = new MyFlowExecutionListener();
        executor.addListener(listener);
        List<ExecutionStep> executionOrder = listener.getExecutionOrder();

        try {
            executor.execute();
        } catch (DataDependencyException e) {
            assertEquals("Injectable Data sampleData cannot be null in flipkart.tef.execution.FlowExecutorTest$SampleEnrichmentBizlogic", e.getMessage());
        }

        int idx = 0;

        idx = assertExecutionOrder(executionOrder, idx, adapterClass);

        assertEquals(executionOrder.get(idx).stage, ExecutionStage.PRE);
        assertEquals(executionOrder.get(idx++).bizlogic, enricherClass);
        assertExecutionOrder(executionOrder, idx, adapterClass);
    }

    @Test
    public void testFlowExecutionWithValidNullData() throws IllegalAccessException, DataDependencyException, InstantiationException, TefExecutionException {
        Class<? extends IBizlogic> adapterClass = SampleDataAdapter.class;
        Class<? extends IBizlogic> enricherClass = SampleEnrichmentBizlogic2.class;

        SampleDataAdapter.ADAPTED = null;
        flowBuilder.add(adapterClass);
        flowBuilder.add(enricherClass);

        assertEquals(2, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(enricherClass));
        assertTrue(flowBuilder.getBizlogics().contains(adapterClass));

        SimpleFlow flow = flowBuilder.build();
        List<Class<? extends IBizlogic>> order = flow.getBizlogics();
        assertEquals(adapterClass, order.get(0));
        assertEquals(enricherClass, order.get(1));

        DataContext dataContext = new DataContext();
        FlowExecutor executor = new FlowExecutor(flow, dataContext, new TestTefContext());

        MyFlowExecutionListener listener = new MyFlowExecutionListener();
        executor.addListener(listener);
        executor.execute();
        List<ExecutionStep> executionOrder = listener.getExecutionOrder();

        int idx = 0;

        idx = assertExecutionOrder(executionOrder, idx, adapterClass);

        assertEquals(executionOrder.get(idx).stage, ExecutionStage.PRE);
        assertEquals(executionOrder.get(idx++).bizlogic, enricherClass);
        assertExecutionOrder(executionOrder, idx, adapterClass);
    }

    /**
     * For an optional injection with no implicit binding, the value is not injected (remains null)
     */
    @Test
    public void testOptionalInjectionWithNoBinding() throws IllegalAccessException, DataDependencyException, InstantiationException, TefExecutionException {
        flowBuilder.add(OptionalInjectionBizlogic1.class);
        SimpleFlow simpleFlow = flowBuilder.build();
        assertEquals(1, simpleFlow.getBizlogics().size());
        assertTrue(simpleFlow.getBizlogics().contains(OptionalInjectionBizlogic1.class));

        DataContext dataContext = new DataContext();
        FlowExecutor executor = new FlowExecutor(simpleFlow, dataContext, new TestTefContext());
        executor.execute();
    }

    /**
     * For an optional injection data is injected from the implicit data binding if its available.
     */
    @Test
    public void testOptionalInjectionWithImplicitBinding() throws IllegalAccessException, DataDependencyException, InstantiationException, TefExecutionException {
        flowBuilder.add(OptionalInjectionBizlogic2.class);
        SimpleFlow simpleFlow = flowBuilder.build();
        assertEquals(1, simpleFlow.getBizlogics().size());
        assertTrue(simpleFlow.getBizlogics().contains(OptionalInjectionBizlogic2.class));

        DataContext dataContext = new DataContext();
        dataContext.put(new DataAdapterResult(new OptionalInjectedData()));
        FlowExecutor executor = new FlowExecutor(simpleFlow, dataContext, new TestTefContext());
        executor.execute();
    }

    /**
     * For an optional injection Data adapter is invoked if its available in the flow.
     */
    @Test
    public void testOptionalInjectionWithDataAdapter() throws IllegalAccessException, DataDependencyException, InstantiationException, TefExecutionException {
        flowBuilder.add(OptionalInjectionBizlogic2.class);
        flowBuilder.add(OptionalInjectedDataAdapter.class);
        SimpleFlow simpleFlow = flowBuilder.build();
        assertEquals(2, simpleFlow.getBizlogics().size());
        assertTrue(simpleFlow.getBizlogics().contains(OptionalInjectionBizlogic2.class));
        assertTrue(simpleFlow.getBizlogics().contains(OptionalInjectedDataAdapter.class));

        MyFlowExecutionListener listener = new MyFlowExecutionListener();
        FlowExecutor executor = new FlowExecutor(simpleFlow, new DataContext(), new TestTefContext());
        executor.addListener(listener);
        executor.execute();

        List<ExecutionStep> executionOrder = listener.getExecutionOrder();

        int idx = 0;

        idx = assertExecutionOrder(executionOrder, idx, OptionalInjectedDataAdapter.class);
        assertEquals(executionOrder.get(idx).stage, ExecutionStage.PRE);
        assertEquals(executionOrder.get(idx++).bizlogic, OptionalInjectionBizlogic2.class);
        idx = assertExecutionOrder(executionOrder, idx, OptionalInjectedDataAdapter.class);
        assertEquals(executionOrder.get(idx).stage, ExecutionStage.POST);
        assertEquals(executionOrder.get(idx++).bizlogic, OptionalInjectionBizlogic2.class);
    }

    /**
     * This test creates a scenario of a data adapter having a data injection, such that
     * it triggers the mutation flow and validates that it has no side-effects.
     *
     * @throws DataDependencyException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test
    public void testDataAdapterWithDataInjection() throws DataDependencyException, IllegalAccessException, InstantiationException, TefExecutionException {
        Class<? extends IBizlogic> adapterClass = SampleDataAdapter.class;
        Class<? extends IBizlogic> adapterClass2 = SampleDataAdapter2.class;
        Class<? extends IBizlogic> bizlogic1Class = SampleBizlogic1.class;

        SampleDataAdapter.ADAPTED = new SampleData();

        flowBuilder.add(adapterClass);
        flowBuilder.add(adapterClass2);
        flowBuilder.add(bizlogic1Class);

        Collection<Class<? extends IBizlogic>> bizlogics = flowBuilder.getBizlogics();
        assertEquals(3, bizlogics.size());
        assertTrue(bizlogics.contains(adapterClass2));
        assertTrue(bizlogics.contains(adapterClass));
        assertTrue(bizlogics.contains(bizlogic1Class));

        SimpleFlow flow = flowBuilder.build();
        assertEquals(3, flow.getBizlogics().size());
        assertEquals(adapterClass, flow.getBizlogics().get(0));
        assertEquals(adapterClass2, flow.getBizlogics().get(1));
        assertEquals(bizlogic1Class, flow.getBizlogics().get(2));

        DataContext dataContext = new DataContext();
        FlowExecutor executor = new FlowExecutor(flow, dataContext, new TestTefContext());

        MyFlowExecutionListener listener = new MyFlowExecutionListener();
        executor.addListener(listener);
        executor.execute();
    }

    /**
     * This test checks whether a data adapter gets multiple calls to execute if
     * its returning null data - i.e. cache is not working
     * P.S https://github.com/flipkart-incubator/tef/issues/8
     */
    @Test
    public void testNullableBizlogic() throws Exception {
        Class<? extends IBizlogic> adapterClass = NullableDataAdapter.class;
        Class<? extends IBizlogic> bizlogic1 = NullableInjectionBizlogic1.class;
        Class<? extends IBizlogic> bizlogic2 = NullableInjectionBizlogic2.class;

        flowBuilder.add(adapterClass);
        flowBuilder.add(bizlogic1);
        flowBuilder.add(bizlogic2);

        assertEquals(3, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(bizlogic1));
        assertTrue(flowBuilder.getBizlogics().contains(bizlogic2));
        assertTrue(flowBuilder.getBizlogics().contains(adapterClass));

        SimpleFlow flow = flowBuilder.build();
        DataContext dataContext = new DataContext();
        FlowExecutor executor = new FlowExecutor(flow, dataContext, new TestTefContext());
        executor.execute();
    }

    public static class NullableDataAdapter extends DataAdapterBizlogic<SampleData> {

        boolean executed;

        @Override
        public SampleData adapt(TefContext tefContext) throws TefExecutionException {
            assertTrue(!executed); //only execute the data adapter once
            executed = true;
            return null;
        }
    }

    public static class NullableInjectionBizlogic1 implements IBizlogic {

        @InjectData(nullable = true)
        SampleData sampleData;

        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {

        }
    }

    public static class NullableInjectionBizlogic2 implements IBizlogic {

        @InjectData(nullable = true)
        SampleData sampleData;

        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {

        }
    }

    public static class SampleData extends MapBaseData {
    }

    public static class SampleData2 extends MapBaseData {
    }

    public static class SampleDataAdapter extends DataAdapterBizlogic<SampleData> {

        public static SampleData ADAPTED = new SampleData();

        @Override
        public SampleData adapt(TefContext tefContext) {
            return ADAPTED;
        }
    }

    public static class SampleDataAdapter2 extends DataAdapterBizlogic<SampleData2> {

        public static SampleData2 ADAPTED = new SampleData2();

        private static int counter = 0;
        @InjectData
        private SampleData sampleData;

        @Override
        public SampleData2 adapt(TefContext tefContext) {
            assertEquals(0, (counter++));
            return ADAPTED;
        }
    }

    public static class SampleBizlogic1 implements IBizlogic {

        @InjectData
        private SampleData sampleData;

        @InjectData
        private SampleData2 sampleData2;

        @Override
        public void execute(TefContext tefContext) {

        }
    }

    public static class SampleEnrichmentBizlogic extends BasicEnrichmentBizlogic {

        @InjectData
        private SampleData sampleData;

        @Override
        public EnrichmentResultData enrich() {
            assertEquals(sampleData, SampleDataAdapter.ADAPTED);
            return null;
        }

        @Override
        public void map(EnrichmentResultData enriched, Object o) {

        }

        @Override
        public Object getTarget() {
            return new Object();
        }
    }

    public static class SampleEnrichmentBizlogic2 extends BasicEnrichmentBizlogic {

        @InjectData(nullable = true)
        private SampleData sampleData;

        @Override
        public EnrichmentResultData enrich() {
            assertEquals(sampleData, SampleDataAdapter.ADAPTED);
            return null;
        }

        @Override
        public void map(EnrichmentResultData enriched, Object o) {

        }

        @Override
        public Object getTarget() {
            return new Object();
        }
    }

    public static class OptionalInjectionBizlogic1 implements IBizlogic {

        @InjectData(optional = true)
        private OptionalInjectedData optionalInjectedData;

        @Override
        public void execute(TefContext tefContext) {
            assertTrue(optionalInjectedData == null);
        }
    }

    public static class OptionalInjectionBizlogic2 implements IBizlogic {

        @InjectData(optional = true)
        private OptionalInjectedData optionalInjectedData;

        @Override
        public void execute(TefContext tefContext) {
            assertTrue(optionalInjectedData != null);
        }
    }

    public static class OptionalInjectedDataAdapter extends DataAdapterBizlogic<OptionalInjectedData> {

        @Override
        public OptionalInjectedData adapt(TefContext tefContext) {
            return new OptionalInjectedData();
        }
    }

    public static class OptionalInjectedData {

    }
}