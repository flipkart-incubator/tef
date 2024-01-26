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

package flipkart.tef.guicebridge;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.matcher.Matchers;
import flipkart.tef.annotations.InjectData;
import flipkart.tef.bizlogics.DataAdapterKey;
import flipkart.tef.bizlogics.DataAdapterResult;
import flipkart.tef.interfaces.InjectableValueProvider;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"unused"})
public class InjectDataGuiceMembersInjectorTest {

    static class TestValueProvider implements InjectableValueProvider {

        Map<DataAdapterKey<?>, DataAdapterResult> dataContext = new HashMap<>();

        TestValueProvider() {
            Long threadId = Thread.currentThread().getId();

            putInDataContext(dataContext, new DataAdapterResult(new SimpleData()));
            putInDataContext(dataContext, new DataAdapterResult(threadId));
        }

        private void putInDataContext(Map<DataAdapterKey<?>, DataAdapterResult> dataContext, DataAdapterResult dataAdapterResult) {
            dataContext.put(dataAdapterResult.getKey(), dataAdapterResult);
        }

        @Override
        public Object getValueToInject(Class<?> fieldType, String name) {
            return dataContext.get(new DataAdapterKey<>(name, fieldType)).getResult();
        }
    }

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

        InjectableValueProvider valueProvider = new TestValueProvider();

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

        try (TefGuiceScope scope = rootInjector.getInstance(TefGuiceScope.class)) {
            InjectableValueProvider valueProvider = new TestValueProvider();
            InjectableValueProvider valueProvider2 = new TestValueProvider();
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
            InjectableValueProvider valueProvider = new TestValueProvider();
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
                InjectableValueProvider valueProvider = new TestValueProvider();
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