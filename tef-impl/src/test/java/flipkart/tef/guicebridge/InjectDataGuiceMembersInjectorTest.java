package flipkart.tef.guicebridge;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import flipkart.tef.annotations.InjectData;
import flipkart.tef.bizlogics.DataAdapterKey;
import flipkart.tef.bizlogics.DataAdapterResult;
import flipkart.tef.exception.TefExecutionException;
import flipkart.tef.execution.DataContext;
import flipkart.tef.execution.InjectableValueProvider;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class InjectDataGuiceMembersInjectorTest {

    @Ignore
    @Test
    public void testRequestScopeBasic() {

        Injector rootInjector = Guice.createInjector(new GuiceBridgeModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bindListener(Matchers.any(), new TypeListenerForDataInjection());
            }
        });


        new TesterThread(rootInjector).run();
        new TesterThread(rootInjector).run();
    }

    @Ignore
    @Test
    public void testRequestScopeThreaded() throws InterruptedException {

        Injector rootInjector = Guice.createInjector(new GuiceBridgeModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bindListener(Matchers.any(), new TypeListenerForDataInjection());
            }
        });

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(new TesterThread(rootInjector));
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }

    static class TesterThread implements Runnable {

        Injector rootInjector;

        public TesterThread(Injector rootInjector) {
            this.rootInjector = rootInjector;
        }

        @Override
        public void run() {
            Long threadId = Thread.currentThread().getId();
            try (TefGuiceScope scope = rootInjector.getInstance(TefGuiceScope.class)) {
                DataContext dataContext = new DataContext();
                dataContext.put(new DataAdapterResult(new SimpleData()));
                dataContext.put(new DataAdapterResult(threadId));
                InjectableValueProvider valueProvider = new InjectableValueProvider() {

                    @Override
                    public Object getValueToInject(Class<?> fieldType, String name) throws TefExecutionException {
                        return dataContext.get(new DataAdapterKey<>(name, fieldType));
                    }
                };

                scope.open(valueProvider);
                SimpleInterface result = rootInjector.getInstance(SampleCallable.class).call();
                assertNotNull("Data injection failed", result.simpleData);
                assertEquals(threadId, result.threadId);
            }
        }
    }

    @TefRequestScoped
    static class SampleCallable implements Callable<SimpleInterface> {
        private SimpleInterface simpleInterface;

        @Inject
        public SampleCallable(SimpleInterface simpleInterface) {
            this.simpleInterface = simpleInterface;
        }

        @Override
        public SimpleInterface call()
        {
            return simpleInterface;
        }
    }

    static class SimpleData{}

    static class SimpleInterface implements Serializable {
        @InjectData
        SimpleData simpleData;
        @InjectData
        Long threadId;
    }
}