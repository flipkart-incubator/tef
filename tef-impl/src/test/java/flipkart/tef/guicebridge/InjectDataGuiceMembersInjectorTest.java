package flipkart.tef.guicebridge;

import flipkart.tef.TestTefContext;
import flipkart.tef.annotations.InjectData;
import flipkart.tef.bizlogics.TefContext;
import org.junit.Assert;
import org.junit.Test;

public class InjectDataGuiceMembersInjectorTest {

    @Test
    public void testRequestScopeBasic(){
        TefContext tefContext = new TestTefContext();
        SimpleInterface instance = tefContext.getInjector().getInstance(SimpleInterface.class);

        Assert.assertNotNull("Data injection failed", instance.simpleData);
    }

    static class SimpleData{}

    static class SimpleInterface {
        @InjectData SimpleData simpleData;
    }
}