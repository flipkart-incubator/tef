/*
 * Copyright [2023] [The Original Author]
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

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import flipkart.tef.interfaces.InjectableValueProvider;

/**
 * Custom guice scope (request-scoped) that injects an instance of
 *
 * @see InjectableValueProvider
 * from reuquest (thread-local). Other injections are passed over to creator.
 * <p>
 * Date: 1/06/22
 */
public class TefGuiceScope implements Scope, AutoCloseable {

    private final ThreadLocal<InjectableValueProvider> threadLocal;

    public TefGuiceScope() {
        threadLocal = new ThreadLocal<>();
    }

    @SuppressWarnings("unchecked")
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> creator) {
        return new Provider<T>() {
            public T get() {
                if(key.getTypeLiteral().getRawType().isAssignableFrom(InjectableValueProvider.class)){
                    Preconditions.checkState(threadLocal.get() != null, "A scoping block is missing");
                    return (T) threadLocal.get();
                } else {
                    return creator.get();
                }
            }

            public String toString() {
                return String.format("%s[%s]", creator, "TefRequestScoped");
            }
        };
    }

    public String toString() {
        return "TefRequestScoped";
    }

    public void open(InjectableValueProvider valueProvider){
        Preconditions.checkState(threadLocal.get() == null, "A scoping block is already in progress");
        threadLocal.set(valueProvider);
    }

    @Override
    public void close() {
        threadLocal.remove();
    }
}
