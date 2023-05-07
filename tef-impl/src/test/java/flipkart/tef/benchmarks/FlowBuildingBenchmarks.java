package flipkart.tef.benchmarks;

import com.google.common.collect.Lists;
import flipkart.tef.TestTefContext;
import flipkart.tef.bizlogics.IBizlogic;
import flipkart.tef.exception.TefExecutionException;
import flipkart.tef.execution.DataContext;
import flipkart.tef.execution.FlowExecutor;
import flipkart.tef.execution.FluentCapabilityBuilder;
import flipkart.tef.flow.SimpleFlow;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FlowBuildingBenchmarks {

    @State(Scope.Benchmark)
    public static class TestState {
        public final SimpleFlow simpleFlow = getTefFlow();

        private static SimpleFlow getTefFlow() {
            System.out.println("Inside getTefFlow");
            return new FluentCapabilityBuilder().withBizlogics(Lists.newArrayList(
                    TestBizlogics.Bizlogic1.class,
                    TestBizlogics.Bizlogic2.class,
                    TestBizlogics.Bizlogic3.class,
                    TestBizlogics.Bizlogic4.class,
                    TestBizlogics.Bizlogic5.class,
                    TestBizlogics.Bizlogic6.class,
                    TestBizlogics.Bizlogic7.class,
                    TestBizlogics.Bizlogic8.class,
                    TestBizlogics.Bizlogic9.class,
                    TestBizlogics.Bizlogic10.class
            )).dataflow();
        }
    }


    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    @Threads(Threads.MAX)
    @Benchmark
    @Warmup(iterations = 1, time = 1000, timeUnit = MILLISECONDS)
    @Measurement(iterations = 2, time = 10000, timeUnit = MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    @BenchmarkMode(Mode.All)
    public void viaTef(TestState testState) throws Exception {
        DataContext dataContext = new DataContext();
        FlowExecutor executor = new FlowExecutor(testState.simpleFlow, dataContext, new TestTefContext());
        executor.execute();

        // With threads
        // FlowBuildingBenchmarks.viaTef  thrpt    2  77061.732          ops/s

        // Without threads
        //FlowBuildingBenchmarks.viaTef  thrpt    2  15136.864          ops/s
    }

    @Threads(Threads.MAX)
    @Benchmark
    @Warmup(iterations = 1, time = 1000, timeUnit = MILLISECONDS)
    @Measurement(iterations = 2, time = 10000, timeUnit = MILLISECONDS)
    @Fork(value = 1, warmups = 1)
    @BenchmarkMode(Mode.All)
    public void bareBizlogics(TestState testState) throws Exception {

        executeBare();

        // With threads
        // FlowBuildingBenchmarks.bareBizlogics  thrpt    2  121265.750          ops/s

        // Without threads
        //FlowBuildingBenchmarks.bareBizlogics  thrpt    2  24207.253          ops/s
    }

    private void executeBare() throws TefExecutionException {
        TestTefContext testTefContext = new TestTefContext();
        IBizlogic bizlogic = new TestBizlogics.Bizlogic1();
        bizlogic.execute(testTefContext);

        bizlogic = new TestBizlogics.Bizlogic2();
        bizlogic.execute(testTefContext);

        bizlogic = new TestBizlogics.Bizlogic3();
        bizlogic.execute(testTefContext);

        bizlogic = new TestBizlogics.Bizlogic4();
        bizlogic.execute(testTefContext);

        bizlogic = new TestBizlogics.Bizlogic5();
        bizlogic.execute(testTefContext);

        bizlogic = new TestBizlogics.Bizlogic6();
        bizlogic.execute(testTefContext);

        bizlogic = new TestBizlogics.Bizlogic7();
        bizlogic.execute(testTefContext);

        bizlogic = new TestBizlogics.Bizlogic8();
        bizlogic.execute(testTefContext);

        bizlogic = new TestBizlogics.Bizlogic9();
        bizlogic.execute(testTefContext);

        bizlogic = new TestBizlogics.Bizlogic10();
        bizlogic.execute(testTefContext);
    }
}
