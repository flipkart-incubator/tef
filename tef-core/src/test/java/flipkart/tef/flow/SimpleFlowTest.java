/*
 * Copyright [2021] [The Original Author]
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

package flipkart.tef.flow;


import com.google.common.collect.HashBiMap;
import flipkart.tef.bizlogics.IBizlogic;
import flipkart.tef.bizlogics.TefContext;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class SimpleFlowTest {


    @Test
    public void testToStringForEmptyFlow() {
        SimpleFlow instance = new SimpleFlow(Collections.emptyList(), HashBiMap.create());
        String expResult = "bizlogics ->\n";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testToStringForNonEmptyFlow() {
        SimpleFlow instance = new SimpleFlow(Arrays.asList(Bizlogic1.class, Bizlogic2.class), HashBiMap.create());
        String expResult = "bizlogics ->\n" +
                "flipkart.tef.flow.SimpleFlowTest$Bizlogic1\n" +
                "flipkart.tef.flow.SimpleFlowTest$Bizlogic2";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    static class Bizlogic1 implements IBizlogic {

        @Override
        public void execute(TefContext tefContext) {

        }
    }

    static class Bizlogic2 implements IBizlogic {

        @Override
        public void execute(TefContext tefContext) {

        }
    }
}