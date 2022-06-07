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

package flipkart.tef.execution;

import com.google.common.reflect.Reflection;
import flipkart.tef.annotations.InjectData;
import flipkart.tef.exception.TefExecutionException;

import java.lang.reflect.Field;

/**
 * This class is used for injecting data into bizlogics
 * 
 * Date: 16/04/21
 */
public class DefaultDataInjector implements DataInjector {

    private static final String INJECTABLE_CANNOT_BE_NULL_MESSAGE = "Injectable Data %s cannot be null in %s";

    @Override
    public void injectData(Object bizlogic, Class<?> bizlogicClass,
                           InjectableValueProvider valueProvider) throws DataDependencyException, IllegalAccessException, TefExecutionException {
        Field[] fields = bizlogicClass.getDeclaredFields();
        for (Field field : fields) {
            InjectData injectable = field.getAnnotation(InjectData.class);
            if (injectable != null) {
                field.setAccessible(true);
                Object valueToInject = valueProvider.getValueToInject(field.getType(), injectable.name());
                if (valueToInject == null) {
                    boolean canBeNull = injectable.nullable() || injectable.optional();
                    if (!canBeNull) {
                        throw new DataDependencyException(String.format(INJECTABLE_CANNOT_BE_NULL_MESSAGE, field.getName(), bizlogicClass.getName()));
                    }
                }
                field.set(bizlogic, valueToInject);
            }
        }

        if (!bizlogicClass.equals(Object.class)) {
            injectData(bizlogic, bizlogicClass.getSuperclass(), valueProvider);
        }
    }
}
