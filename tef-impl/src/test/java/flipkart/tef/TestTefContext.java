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

package flipkart.tef;

import flipkart.tef.bizlogics.TefContext;
import flipkart.tef.execution.DefaultDataInjector;
import flipkart.tef.interfaces.BizlogicInstanceProvider;
import flipkart.tef.interfaces.DataInjector;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * This class creates a test version of TefContext
 * <p>
 * Date: 17/12/21
 */
public class TestTefContext extends TefContext {

    public TestTefContext() {
        super(new HashMap<>(), new TestBizlogicInstanceProvider(), System.out::println);
    }

    static class TestBizlogicInstanceProvider implements BizlogicInstanceProvider {

        Map<Class<?>, Object> crudeDI;

        TestBizlogicInstanceProvider() {
            crudeDI = new HashMap<>();
            crudeDI.put(DataInjector.class, new DefaultDataInjector());
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public <T> T getInstance(Class<T> clazz) {
            return (T) crudeDI.computeIfAbsent(clazz, (c) -> {
                try {
                    Constructor<?> declaredConstructor = c.getDeclaredConstructor();
                    declaredConstructor.setAccessible(true);
                    return declaredConstructor.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                throw new RuntimeException("Unable to create instance of " + clazz.getName());
            });
        }
    }
}
