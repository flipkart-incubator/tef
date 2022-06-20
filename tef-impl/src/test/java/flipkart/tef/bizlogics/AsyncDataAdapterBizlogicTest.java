package flipkart.tef.bizlogics;

import com.google.common.collect.Queues;
import flipkart.tef.TestTefContext;
import flipkart.tef.annotations.EmitData;
import flipkart.tef.annotations.InjectData;
import flipkart.tef.exception.TefExecutionException;
import flipkart.tef.execution.DataContext;
import flipkart.tef.execution.DataDependencyException;
import flipkart.tef.execution.FlowExecutor;
import flipkart.tef.execution.FluentCapabilityBuilder;
import flipkart.tef.execution.MyFlowExecutionListener;
import flipkart.tef.flow.SimpleFlow;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AsyncDataAdapterBizlogicTest {

    private static ThreadPoolExecutor threadPoolExecutor;

    private FluentCapabilityBuilder flowBuilder;

    @Before
    public void setUp() {
        flowBuilder = new FluentCapabilityBuilder();
        BlockingQueue<Runnable> blockingQueue = Queues.newArrayBlockingQueue(5);
        threadPoolExecutor = new ThreadPoolExecutor(5, 5, 100, TimeUnit.MILLISECONDS, blockingQueue, (r, executor) -> {
            r.run();
        });
    }

    @Test
    public void testAsyncDataInjection() throws TefExecutionException, DataDependencyException, IllegalAccessException, InstantiationException {


        flowBuilder.withAdapter(Sample1AsyncDataAdapterBizlogic.class);
        flowBuilder.withAdapter(Sample2AsyncDataAdapterBizlogic.class);
        flowBuilder.withAdapter(Sample3AsyncDataAdapterBizlogic.class);
        flowBuilder.withAdapter(Sample4AsyncDataAdapterBizlogic.class);
        flowBuilder.withBizlogic(SimpleBizlogic.class);
        SimpleFlow dataflow = flowBuilder.dataflow();

        assertEquals(5, dataflow.getBizlogics().size());
        assertTrue(dataflow.getBizlogics().contains(Sample1AsyncDataAdapterBizlogic.class));
        assertTrue(dataflow.getBizlogics().contains(Sample2AsyncDataAdapterBizlogic.class));
        assertTrue(dataflow.getBizlogics().contains(Sample3AsyncDataAdapterBizlogic.class));
        assertTrue(dataflow.getBizlogics().contains(Sample4AsyncDataAdapterBizlogic.class));
        assertTrue(dataflow.getBizlogics().contains(SimpleBizlogic.class));

        DataContext dataContext = new DataContext();
        FlowExecutor executor = new FlowExecutor(dataflow, dataContext, new TestTefContext());

        MyFlowExecutionListener listener = new MyFlowExecutionListener();
        executor.addListener(listener);
        executor.execute();
    }

    static class SimpleBizlogic implements IBizlogic {

        // Sleeps and returns
        @InjectData(name = "1")
        Future<Optional<String>> asyncResult1;

        // Sleeps and returns with bubbleException=true, but does not throw an exception
        @InjectData(name = "2")
        Future<Optional<String>> asyncResult2;

        // throws exception with bubbleException=true
        @InjectData(name = "3")
        Future<Optional<String>> asyncResult3;

        // throws exception without bubbleException=true
        @InjectData(name = "4")
        Future<Optional<String>> asyncResult4;

        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {
            // 100ms sleep in workers ensures result is not ready instantly
            assertFalse(asyncResult1.isDone());
            assertFalse(asyncResult2.isDone());
            assertFalse(asyncResult3.isDone());
            assertFalse(asyncResult4.isDone());

            // 200ms sleep ensures results are ready
            sleep(200);
            assertTrue(asyncResult1.isDone());
            assertTrue(asyncResult2.isDone());
            assertTrue(asyncResult3.isDone());
            assertTrue(asyncResult4.isDone());

            try {
                // assert on results
                assertEquals("1", asyncResult1.get().get());
                assertEquals("2", asyncResult2.get().get());
                // #4 does not return a result since it throws an exception
                assertFalse(asyncResult4.get().isPresent());
            } catch (Exception e) {
                fail("Unexpected exception");
            }

            try {
                // #3 throws an exception with bubbleException=3
                asyncResult3.get();
                fail("Runtime exception was expected");
            } catch (RuntimeException | InterruptedException | ExecutionException e) {
                assertEquals("java.lang.RuntimeException: 3", e.getMessage());
            }
        }
    }

    static void sleep(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void sleep() {
        sleep(100);
    }


    @EmitData(name = "1")
    static class Sample1AsyncDataAdapterBizlogic extends AsyncDataAdapterBizlogic<Future<Optional<String>>, String> {

        public Sample1AsyncDataAdapterBizlogic() {
            super(AsyncDataAdapterBizlogicTest.threadPoolExecutor);
        }

        @Override
        public String getResult(TefContext tefContext) throws TefExecutionException {
            sleep();
            return "1";
        }
    }

    @EmitData(name = "2")
    static class Sample2AsyncDataAdapterBizlogic extends AsyncDataAdapterBizlogic<Future<Optional<String>>, String> {

        public Sample2AsyncDataAdapterBizlogic() {
            super(AsyncDataAdapterBizlogicTest.threadPoolExecutor, true);
        }

        @Override
        public String getResult(TefContext tefContext) throws TefExecutionException {
            sleep();
            return "2";
        }
    }

    @EmitData(name = "3")
    static class Sample3AsyncDataAdapterBizlogic extends AsyncDataAdapterBizlogic<Future<Optional<String>>, String> {

        public Sample3AsyncDataAdapterBizlogic() {
            super(AsyncDataAdapterBizlogicTest.threadPoolExecutor, true);
        }

        @Override
        public String getResult(TefContext tefContext) throws TefExecutionException {
            sleep();
            throw new RuntimeException("3");
        }
    }

    @EmitData(name = "4")
    static class Sample4AsyncDataAdapterBizlogic extends AsyncDataAdapterBizlogic<Future<Optional<String>>, String> {

        public Sample4AsyncDataAdapterBizlogic() {
            super(AsyncDataAdapterBizlogicTest.threadPoolExecutor);
        }

        @Override
        public String getResult(TefContext tefContext) throws TefExecutionException {
            sleep();
            throw new RuntimeException("4");
        }
    }
}