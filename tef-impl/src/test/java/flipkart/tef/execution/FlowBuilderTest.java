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

import flipkart.tef.annotations.DependsOn;
import flipkart.tef.annotations.InjectData;
import flipkart.tef.bizlogics.DataAdapterBizlogic;
import flipkart.tef.bizlogics.DataAdapterKey;
import flipkart.tef.bizlogics.IBizlogic;
import flipkart.tef.bizlogics.TefContext;
import flipkart.tef.capability.AdapterConflictRuntimeException;
import flipkart.tef.capability.model.MapBaseData;
import flipkart.tef.exception.TefExecutionException;
import flipkart.tef.exceptions.UnableToResolveDataFromAdapterRuntimeException;
import flipkart.tef.flow.SimpleFlow;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("unchecked")
public class FlowBuilderTest {

    /**
     * Helper method to create anonymous Data Adapter Keys
     *
     * @param clazz
     * @return
     */
    private <T> DataAdapterKey<T> getResultKey(Class<T> clazz) {
        return new DataAdapterKey<T>("", clazz);
    }

    @Test
    public void testIslandBizlogics() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleValidator4.class);
        flowBuilder.add(SimpleValidator5.class);
        flowBuilder.add(SimpleValidator6.class);

        assertEquals(3, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator4.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator5.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator6.class));

        SimpleFlow flow = flowBuilder.build();
        assertEquals(3, flow.getBizlogics().size());
        assertTrue(flow.getBizlogics().contains(SimpleValidator4.class));
        assertTrue(flow.getBizlogics().contains(SimpleValidator5.class));
        assertTrue(flow.getBizlogics().contains(SimpleValidator6.class));
    }

    @Test
    public void testDataDependencyBasic() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleDataAdapter.class);
        flowBuilder.build();

        assertEquals(1, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(SimpleDataAdapter.class));
        assertEquals(1, flowBuilder.getDataAdapterMap().size());
        assertEquals(flowBuilder.getDataAdapterMap().get(getResultKey(SimpleData.class)).getName(), SimpleDataAdapter.class.getName());

    }

    @Test
    public void testDataDependencyWithDuplicates() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleDataAdapter.class);
        flowBuilder.add(SimpleDataAdapter.class);
        flowBuilder.build();

        assertEquals(1, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(SimpleDataAdapter.class));
        assertEquals(1, flowBuilder.getDataAdapterMap().size());
        assertEquals(flowBuilder.getDataAdapterMap().get(getResultKey(SimpleData.class)).getName(), SimpleDataAdapter.class.getName());

    }

    @Test
    public void testBizlogicRuntimeDependency() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleDataAdapter.class, SimpleValidator.class);
        flowBuilder.build();

        assertTrue(flowBuilder.getBizlogics().size() == 2);
        assertTrue(flowBuilder.getBizlogics().contains(SimpleDataAdapter.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator.class));
        assertEquals(1, flowBuilder.getDataAdapterMap().size());
        assertEquals(SimpleDataAdapter.class, flowBuilder.getDataAdapterMap().get(getResultKey(SimpleData.class)));
        assertEquals(1, flowBuilder.getReverseBizlogicDependencyMap().get(SimpleValidator.class).size());
        assertTrue(flowBuilder.getReverseBizlogicDependencyMap().get(SimpleValidator.class).contains(SimpleDataAdapter.class));
    }

    @Test
    public void testBizlogicCompileDependency() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleEnricher.class);
        flowBuilder.build();

        assertTrue(flowBuilder.getBizlogics().size() == 2);
        assertTrue(flowBuilder.getBizlogics().contains(SimpleEnricher.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator.class));
        assertEquals(1, flowBuilder.getBizlogicDependencyMap().get(SimpleEnricher.class).size());
        assertEquals(1, flowBuilder.getReverseBizlogicDependencyMap().get(SimpleValidator.class).size());
        assertTrue(flowBuilder.getReverseBizlogicDependencyMap().get(SimpleValidator.class).contains(SimpleEnricher.class));
    }

    @Test
    public void testBizlogicDataDependency() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleEnricher2.class, SimpleDataAdapter.class);
        flowBuilder.build();

        assertTrue(flowBuilder.getBizlogics().size() == 2);
        assertTrue(flowBuilder.getBizlogics().contains(SimpleEnricher2.class));
        assertTrue(flowBuilder.getDataDependencyMap().get(SimpleEnricher2.class).size() == 1);
        assertTrue(flowBuilder.getDataDependencyMap().get(SimpleEnricher2.class).stream()
                .findFirst().get().getDataAdapterKey()
                .equals(getResultKey(SimpleData.class)));
    }

    @Test
    public void testImplicitControlDependencyWithoutStartNode() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleValidator2.class);

        assertEquals(1, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator2.class));

        try {
            flowBuilder.build();
            fail("Validation Error was expected");
        } catch (IllegalArgumentException e) {
            assertEquals(FlowBuilder.Messages.COULD_NOT_DEDUCE_THE_STARTING_STEP, e.getMessage());
        }
    }

    @Test
    public void testCyclicDependency() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleValidator2.class);
        flowBuilder.add(SimpleValidator4.class);
        flowBuilder.add(SimpleDataAdapter.class);
        flowBuilder.add(SimpleEnricher2.class);

        assertEquals(4, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator2.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator4.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleDataAdapter.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleEnricher2.class));

        try {
            flowBuilder.build();
            fail("Validation Error was expected");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Cyclic Graphs are not supported."));
            assertTrue(e.getMessage().contains("flipkart.tef.execution.FlowBuilderTest$SimpleValidator3"));
            assertTrue(e.getMessage().contains("flipkart.tef.execution.FlowBuilderTest$SimpleValidator2"));
        }
    }

    @Test
    public void testCyclicDataDependency() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleValidator6.class);
        flowBuilder.add(DataAdapter1.class);
        flowBuilder.add(DataAdapter2.class);

        assertEquals(3, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(DataAdapter1.class));
        assertTrue(flowBuilder.getBizlogics().contains(DataAdapter2.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator6.class));

        try {
            flowBuilder.build();
            fail("Validation Error was expected");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Cyclic Graphs are not supported."));
            assertTrue(e.getMessage().contains("flipkart.tef.execution.FlowBuilderTest$DataAdapter2"));
            assertTrue(e.getMessage().contains("flipkart.tef.execution.FlowBuilderTest$DataAdapter1"));
        }
    }

    @Test
    public void testMissingStartNode() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleValidator2.class, SimpleValidator3.class);

        assertEquals(2, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator2.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator3.class));

        try {
            flowBuilder.build();
            fail("Validation Error was expected");
        } catch(IllegalArgumentException e){
            assertEquals(FlowBuilder.Messages.COULD_NOT_DEDUCE_THE_STARTING_STEP, e.getMessage());
        }
    }

    @Test
    public void testBasicFlow() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleValidator4.class, SimpleValidator5.class);
        flowBuilder.add(SimpleValidator5.class, SimpleValidator6.class);
        SimpleFlow flow = flowBuilder.build();

        assertEquals(3, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator4.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator5.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator6.class));
        assertEquals(1, flowBuilder.getBizlogicDependencyMap().get(SimpleValidator4.class).size());
        assertEquals(1, flowBuilder.getBizlogicDependencyMap().get(SimpleValidator5.class).size());
        assertEquals(0, flowBuilder.getBizlogicDependencyMap().get(SimpleValidator6.class).size());
        assertEquals(1, flowBuilder.getReverseBizlogicDependencyMap().get(SimpleValidator6.class).size());
        assertEquals(1, flowBuilder.getReverseBizlogicDependencyMap().get(SimpleValidator5.class).size());
        assertEquals(0, flowBuilder.getReverseBizlogicDependencyMap().get(SimpleValidator4.class).size());

        List<Class<? extends IBizlogic>> order = flow.getBizlogics();
        assertEquals(SimpleValidator6.class, order.get(0));
        assertEquals(SimpleValidator5.class, order.get(1));
        assertEquals(SimpleValidator4.class, order.get(2));
    }

    @Test
    public void testBasicFlow2() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleValidator4.class, SimpleValidator5.class, SimpleValidator6.class);
        SimpleFlow flow = flowBuilder.build();

        assertEquals(3, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator4.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator5.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator6.class));
        assertEquals(2, flowBuilder.getBizlogicDependencyMap().get(SimpleValidator4.class).size());
        assertEquals(0, flowBuilder.getBizlogicDependencyMap().get(SimpleValidator5.class).size());
        assertEquals(0, flowBuilder.getBizlogicDependencyMap().get(SimpleValidator6.class).size());
        assertEquals(1, flowBuilder.getReverseBizlogicDependencyMap().get(SimpleValidator6.class).size());
        assertEquals(1, flowBuilder.getReverseBizlogicDependencyMap().get(SimpleValidator5.class).size());
        assertEquals(0, flowBuilder.getReverseBizlogicDependencyMap().get(SimpleValidator4.class).size());

        List<Class<? extends IBizlogic>> order = flow.getBizlogics();
        // Since 5 and 6 are pre-requisites for 4, any one of them can get executed before 4.
        assertTrue(order.get(0).equals(SimpleValidator6.class) || order.get(0).equals(SimpleValidator5.class));
        assertTrue(order.get(1).equals(SimpleValidator6.class) || order.get(1).equals(SimpleValidator5.class));
        assertEquals(SimpleValidator4.class, order.get(2));
    }

    @Test
    public void testBasicFlow3() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleValidator4.class, SimpleValidator6.class);
        flowBuilder.add(SimpleValidator5.class, SimpleValidator6.class);
        SimpleFlow flow = flowBuilder.build();

        assertEquals(3, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator4.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator5.class));
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator6.class));
        assertEquals(1, flowBuilder.getBizlogicDependencyMap().get(SimpleValidator4.class).size());
        assertEquals(1, flowBuilder.getBizlogicDependencyMap().get(SimpleValidator5.class).size());
        assertEquals(0, flowBuilder.getBizlogicDependencyMap().get(SimpleValidator6.class).size());
        assertEquals(2, flowBuilder.getReverseBizlogicDependencyMap().get(SimpleValidator6.class).size());
        assertEquals(0, flowBuilder.getReverseBizlogicDependencyMap().get(SimpleValidator5.class).size());
        assertEquals(0, flowBuilder.getReverseBizlogicDependencyMap().get(SimpleValidator4.class).size());

        List<Class<? extends IBizlogic>> order = flow.getBizlogics();
        assertEquals(SimpleValidator6.class, order.get(0));
        assertEquals(SimpleValidator5.class, order.get(1));
        assertEquals(SimpleValidator4.class, order.get(2));
    }

    @Test
    public void testDataDependencyInFlow() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleEnricher2.class);
        flowBuilder.add(SimpleDataAdapter.class);
        SimpleFlow flow = flowBuilder.build();

        assertEquals(2, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(SimpleDataAdapter.class));
        assertEquals(1, flowBuilder.getDataAdapterMap().size());
        assertEquals(SimpleDataAdapter.class, flowBuilder.getDataAdapterMap().get(getResultKey(SimpleData.class)));

        List<Class<? extends IBizlogic>> order = flow.getBizlogics();
        assertEquals(SimpleDataAdapter.class, order.get(0));
        assertEquals(SimpleEnricher2.class, order.get(1));
    }

    @Test
    public void testDataDependencyAbsentInFlow() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleEnricher2.class);

        assertEquals(1, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(SimpleEnricher2.class));

        try {
            flowBuilder.build();
            fail("Validation Error was expected");
        } catch (IllegalArgumentException e) {
            assertEquals("Data Adapter not resolved for  flipkart.tef.execution.FlowBuilderTest.SimpleData in bizlogic flipkart.tef.execution.FlowBuilderTest$SimpleEnricher2", e.getMessage());
        }
    }

    @Test
    public void testNamedDataDependencyAbsentInFlow() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleValidator7.class);

        assertEquals(1, flowBuilder.getBizlogics().size());
        assertTrue(flowBuilder.getBizlogics().contains(SimpleValidator7.class));

        try {
            flowBuilder.build();
            fail("Validation Error was expected");
        } catch (IllegalArgumentException e) {
            assertEquals("Data Adapter not resolved for simpleData flipkart.tef.execution.FlowBuilderTest.SimpleData in bizlogic flipkart.tef.execution.FlowBuilderTest$SimpleValidator7", e.getMessage());
        }
    }

    @Test
    public void testConflictingDataAdapters() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SimpleDataAdapter.class);
        flowBuilder.add(SimpleDataAdapter2.class);
        try {
            flowBuilder.build();
            fail("Validation Error was expected");
        } catch (AdapterConflictRuntimeException e) {
            // No-op
        }
    }

    @Test
    public void testComplexDataAdapter() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(DataAdapter3.class);
        SimpleFlow simpleFlow = flowBuilder.build();
        assertEquals(1, simpleFlow.getBizlogics().size());
        assertTrue(simpleFlow.getBizlogics().contains(DataAdapter3.class));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testDataAdapterExtension() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SubDataAdapter.class);
        SimpleFlow simpleFlow = flowBuilder.build();
        assertEquals(1, simpleFlow.getBizlogics().size());
        assertTrue(simpleFlow.getBizlogics().contains(SubDataAdapter.class));
        assertEquals(SimpleData.class, simpleFlow.getDataAdapterMap().keySet().stream().findFirst().get().getResultClass());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testDataAdapterExtension2() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(SubSubDataAdapter.class);
        SimpleFlow simpleFlow = flowBuilder.build();
        assertEquals(1, simpleFlow.getBizlogics().size());
        assertTrue(simpleFlow.getBizlogics().contains(SubSubDataAdapter.class));
        assertEquals(SimpleData.class, simpleFlow.getDataAdapterMap().keySet().stream().findFirst().get().getResultClass());
    }

    @Test
    public void testDataAdapterExtensionWithoutTypeParam() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(DataAdapterWithoutTypeParam.class);
        try {
            flowBuilder.build();
            fail("UnableToResolveDataFromAdapterRuntimeException was expected");
        } catch (UnableToResolveDataFromAdapterRuntimeException e) {
            // No-op
        }
    }

    /**
     * This test creates a scenario where there is am ambiguity on the start node that will be picked (B or C)
     * Since both of them have 0 dependencies. Test then asserts that the
     * lexicographical order of the class name is picked
     */
    @Test
    public void testSortingAtStart() {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(BizlogicA.class);
        flowBuilder.add(BizlogicB.class);
        flowBuilder.add(BizlogicC.class);
        SimpleFlow simpleFlow = flowBuilder.build();
        assertEquals(3, simpleFlow.getBizlogics().size());
        assertEquals(BizlogicB.class, simpleFlow.getBizlogics().get(0));
        assertEquals(BizlogicC.class, simpleFlow.getBizlogics().get(1));
        assertEquals(BizlogicA.class, simpleFlow.getBizlogics().get(2));
    }


    class SimpleData extends MapBaseData {

    }

    class SimpleDataAdapter extends DataAdapterBizlogic<SimpleData> {

        @Override
        public SimpleData adapt(TefContext tefContext) {
            return new SimpleData();
        }
    }

    class SimpleDataAdapter2 extends DataAdapterBizlogic<SimpleData> {

        @Override
        public SimpleData adapt(TefContext tefContext) {
            return new SimpleData();
        }
    }

    class SimpleValidator implements IBizlogic {

        @Override
        public void execute(TefContext tefContext) {

        }
    }

    @DependsOn(SimpleValidator.class)
    class SimpleEnricher implements IBizlogic {

        @Override
        public void execute(TefContext tefContext) {

        }
    }

    class SimpleEnricher2 implements IBizlogic {
        @InjectData
        private SimpleData simpleData;

        @Override
        public void execute(TefContext tefContext) {

        }
    }

    @DependsOn(SimpleValidator3.class)
    class SimpleValidator2 implements IBizlogic {
        @Override
        public void execute(TefContext tefContext) {

        }
    }

    @DependsOn(SimpleValidator2.class)
    class SimpleValidator3 implements IBizlogic {

        @Override
        public void execute(TefContext tefContext) {

        }
    }

    class SimpleValidator4 implements IBizlogic {

        @Override
        public void execute(TefContext tefContext) {

        }
    }

    class SimpleValidator5 implements IBizlogic {

        @Override
        public void execute(TefContext tefContext) {

        }
    }

    class SimpleValidator6 implements IBizlogic {

        @Override
        public void execute(TefContext tefContext) {

        }
    }

    class SimpleValidator7 implements IBizlogic {

        @InjectData(name = "simpleData")
        private SimpleData simpleData;

        @Override
        public void execute(TefContext tefContext) {

        }
    }

    class CyclicData1 {

    }

    class CyclicData2 {

    }

    class DataAdapter1 extends DataAdapterBizlogic<CyclicData1> {

        @InjectData
        CyclicData2 cyclicData2;

        @Override
        public CyclicData1 adapt(TefContext tefContext) {
            return null;
        }
    }

    class DataAdapter2 extends DataAdapterBizlogic<CyclicData2> {

        @InjectData
        CyclicData1 cyclicData1;

        @Override
        public CyclicData2 adapt(TefContext tefContext) {
            return null;
        }
    }

    class DataAdapter3 extends DataAdapterBizlogic<Map<String, String>> {

        @Override
        public Map<String, String> adapt(TefContext tefContext) {
            return null;
        }
    }

    class OtherContext {

    }

    abstract class SuperDataAdapter<T> extends DataAdapterBizlogic<T> {

        @Override
        public T adapt(TefContext tefContext) {
            return adapt(tefContext.getAdditionalContext("other", OtherContext.class));
        }

        public abstract T adapt(OtherContext otherContext);

    }

    abstract class SubDataAdapter extends SuperDataAdapter<SimpleData> {

    }

    class SubSubDataAdapter extends SubDataAdapter {
        @Override
        public SimpleData adapt(OtherContext otherContext) {
            return new SimpleData();
        }
    }

    class DataAdapterWithoutTypeParam extends DataAdapterBizlogic {

        @Override
        public Object adapt(TefContext tefContext) throws TefExecutionException {
            return null;
        }
    }

    @DependsOn(BizlogicC.class)
    class BizlogicA implements IBizlogic {

        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {

        }
    }

    class BizlogicB implements IBizlogic {

        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {

        }
    }

    class BizlogicC implements IBizlogic {

        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {

        }
    }

}