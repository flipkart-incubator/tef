package flipkart.tef.guicebridge;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.matcher.Matchers;
import flipkart.tef.annotations.InjectData;
import flipkart.tef.bizlogics.DataAdapterKey;
import flipkart.tef.bizlogics.DataAdapterResult;
import flipkart.tef.exception.TefExecutionException;
import flipkart.tef.execution.DataContext;
import flipkart.tef.execution.InjectableValueProvider;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class InjectDataGuiceMembersInjectorTest {

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

    @Test
    public void testRequestScopeWithoutEnteringScope() {

        Injector rootInjector = Guice.createInjector(new GuiceBridgeModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bindListener(Matchers.any(), new TypeListenerForDataInjection());
            }
        });

        Long threadId = Thread.currentThread().getId();
        DataContext dataContext = new DataContext();
        dataContext.put(new DataAdapterResult(new SimpleData()));
        dataContext.put(new DataAdapterResult(threadId));
        InjectableValueProvider valueProvider = new InjectableValueProvider() {

            @Override
            public Object getValueToInject(Class<?> fieldType, String name) throws TefExecutionException {
                return dataContext.get(new DataAdapterKey<>(name, fieldType));
            }
        };
        try {
            rootInjector.getInstance(SimpleInterface.class);
            Assert.fail("Injection should have failed");
        } catch (ProvisionException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            assertEquals("A scoping block is missing", e.getCause().getMessage());
        }
    }

    @Test
    public void testRequestScopeWithEnteringScopeTwice() {

        Injector rootInjector = Guice.createInjector(new GuiceBridgeModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bindListener(Matchers.any(), new TypeListenerForDataInjection());
            }
        });

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

            InjectableValueProvider valueProvider2 = new InjectableValueProvider() {

                @Override
                public Object getValueToInject(Class<?> fieldType, String name) throws TefExecutionException {
                    return dataContext.get(new DataAdapterKey<>(name, fieldType));
                }
            };

            scope.open(valueProvider);
            try {
                scope.open(valueProvider2);
            } catch (IllegalStateException e) {
                assertEquals("A scoping block is already in progress", e.getMessage());
            }
        }
    }


    @Test
    public void testSubTypeMatcher() {

        Injector rootInjector = Guice.createInjector(new GuiceBridgeModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bindListener(new SubclassOrAnnotationMatcher(Serializable.class), new TypeListenerForDataInjection());
            }
        });


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

            // Implementations of Serializable
            SimpleInterface2 result = rootInjector.getInstance(SimpleInterface2.class);
            assertNotNull("Data injection failed", result.simpleData);
            assertEquals(threadId, result.threadId);

            // Annotated with TefRequestScope
            SimpleInterface3 result3 = rootInjector.getInstance(SimpleInterface3.class);
            assertNotNull("Data injection failed", result3.simpleData);
            assertEquals(threadId, result3.threadId);

            // Vanilla class
            SimpleInterface result1 = rootInjector.getInstance(SimpleInterface.class);
            assertNull("Data injection should have failed", result1.simpleData);
            assertNull("Data injection should have failed", result1.threadId);
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
                SimpleInterface result = rootInjector.getInstance(SimpleInterface.class);
                assertNotNull("Data injection failed", result.simpleData);
                assertEquals(threadId, result.threadId);
            }
        }
    }

    static class SimpleData {
    }


    static class SimpleInterface {
        @InjectData
        private SimpleData simpleData;
        @InjectData
        private Long threadId;
    }


    static class SimpleInterface2 implements Serializable {
        @InjectData
        private SimpleData simpleData;
        @InjectData
        private Long threadId;
    }

    @TefRequestScoped
    static class SimpleInterface3 {
        @InjectData
        private SimpleData simpleData;
        @InjectData
        private Long threadId;
    }
}