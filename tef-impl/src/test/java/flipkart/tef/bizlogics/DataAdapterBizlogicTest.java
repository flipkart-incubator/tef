package flipkart.tef.bizlogics;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.internal.BytecodeGen;
import com.google.inject.matcher.Matchers;
import flipkart.tef.annotations.EmitData;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataAdapterBizlogicTest {

    @EmitData(name = "testData")
    static class TestDataAdapterBizlogic extends DataAdapterBizlogic<Object> {
        @Override
        public Object adapt(TefContext tefContext) {
            return null;
        }
    }

    static class TestDataAdapterBizlogic1 extends DataAdapterBizlogic<Object> {
        @Override
        public Object adapt(TefContext tefContext) {
            return null;
        }
    }

    @Test
    public void testGetEmittedDataName() {
        //setup
        Class<? extends DataAdapterBizlogic> clazz = TestDataAdapterBizlogic.class;

        //test
        String emittedDataName = DataAdapterBizlogic.getEmittedDataName(clazz);

        //validate
        assertEquals("testData", emittedDataName);
    }

    @Test
    public void testGetEmittedDataNameForAnnotationAbsence() {
        //setup
        Class<? extends DataAdapterBizlogic> clazz = TestDataAdapterBizlogic1.class;

        //test
        String emittedDataName = DataAdapterBizlogic.getEmittedDataName(clazz);

        //validate
        assertEquals("", emittedDataName);
    }

    @Test
    public void testGetEmittedDataNameWithGuiceProxy() {
        // setup
        Injector injector = Guice.createInjector(new GuiceModule());
        TestDataAdapterBizlogic dataAdapterBizlogic = injector.getInstance(TestDataAdapterBizlogic.class);

        // test
        String emittedDataName = DataAdapterBizlogic.getEmittedDataName(dataAdapterBizlogic.getClass());

        // validate
        assertTrue(dataAdapterBizlogic.getClass().getName().contains(BytecodeGen.ENHANCER_BY_GUICE_MARKER));
        assertEquals("testData", emittedDataName);
    }

    class GuiceModule extends AbstractModule {
        @Override
        protected void configure() {
            bindInterceptor(
                    Matchers.subclassesOf(TestDataAdapterBizlogic.class),
                    Matchers.any(),
                    new CustomInterceptor()
            );
        }
    }

    public class CustomInterceptor implements MethodInterceptor {
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            // Log method details before invocation
            System.out.println("Before test method invocation");

            // Proceed with the actual method invocation
            Object result = invocation.proceed();

            // Log method details after invocation
            System.out.println("After test method invocation");
            return result;
        }
    }
}