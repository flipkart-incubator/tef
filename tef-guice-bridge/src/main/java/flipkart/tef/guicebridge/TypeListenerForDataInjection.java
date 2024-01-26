/*
 * Copyright [2024] [The Original Author]
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

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import flipkart.tef.annotations.InjectData;

import java.lang.reflect.Field;

/**
 * TypeListener to process instances which are using the @InjectData annotation.
 *
 * Date: 31/05/22
 */
public class TypeListenerForDataInjection implements TypeListener {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
        Class<?> bizlogicClass = typeLiteral.getRawType();

        while ((!bizlogicClass.equals(Object.class))) {
            Field[] fields = bizlogicClass.getDeclaredFields();
            for (Field field : fields) {
                if(field.isAnnotationPresent(InjectData.class)) {
                    InjectData injectable = field.getAnnotation(InjectData.class);

                    /*
                    Instance of `InjectDataGuiceMembersInjector` is fetched from provider instead of creating via new
                    to let guice provide a handle to Injector inside `InjectDataGuiceMembersInjector`.

                    That comes in handy to fetch the requestScopedBinding that is used to
                    inject the instance of `InjectableValueProvider`
                     */
                    InjectDataGuiceMembersInjector membersInjector = typeEncounter.getProvider(InjectDataGuiceMembersInjector.class).get();
                    membersInjector.setField(field);
                    membersInjector.setInjectionName(injectable.name());
                    typeEncounter.register(membersInjector);
                }
            }
            bizlogicClass = bizlogicClass.getSuperclass();
        }
    }
}
