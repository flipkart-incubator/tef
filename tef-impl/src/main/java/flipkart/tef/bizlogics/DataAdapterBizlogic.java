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

package flipkart.tef.bizlogics;

import flipkart.tef.annotations.EmitData;
import flipkart.tef.annotations.InjectData;
import flipkart.tef.exception.TefExecutionException;
import flipkart.tef.interfaces.MutationListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A specialized bizlogic that is responsible for emitting data
 * that can be used by other bizlogics.
 *
 * 
 * Date: 19/06/20
 * Time: 4:36 PM
 */
public abstract class DataAdapterBizlogic<T> implements IDataBizlogic<T>, MutationListener {

    /**
     * This is a fieldCache to save cost of reflection. The fieldCache is then used to handle mutations
     * on injected members.
     */
    private final Map<DataAdapterKey<?>, Field> fieldCache;
    private final String emittedDataName;
    private final Class<T> resultType;

    private T result;
    private boolean resultComputed = false;

    public DataAdapterBizlogic() {
        fieldCache = buildCacheOfMutableFields();
        emittedDataName = getEmittedDataName(this.getClass());
        resultType = getResultType();
    }

    @SuppressWarnings("unchecked")
    protected Class<T> getResultType() {
        try {
            Method method = this.getClass().getMethod("adapt", TefContext.class);
            return (Class<T>) method.getReturnType();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Interface error in Tef - Adapt Method changed", e);
        }
    }

    private Map<DataAdapterKey<?>, Field> buildCacheOfMutableFields() {

        Map<DataAdapterKey<?>, Field> cache = new HashMap<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            InjectData annotation = field.getAnnotation(InjectData.class);
            if (annotation != null && annotation.mutable()) {
                cache.put(new DataAdapterKey<>(annotation.name(), field.getType()), field);
            }
        }

        return cache;
    }

    @SuppressWarnings("rawtypes")
    public static String getEmittedDataName(Class<? extends DataAdapterBizlogic> clazz) {
        EmitData emitData = clazz.getAnnotation(EmitData.class);
        if (emitData != null) {
            return emitData.name();
        } else {
            return "";
        }
    }

    @Override
    public final Optional<DataAdapterResult> executeForData(TefContext tefContext) throws TefExecutionException {
        if (!resultComputed) {
            result = adapt(tefContext);
            resultComputed = true;
        }
        return Optional.of(new DataAdapterResult(result, name(), resultType));
    }

    /**
     * This method is invoked when an Injected data which is marked for mutation, changes.
     *
     * @param object Mutated Object
     */
    @Override
    public final void mutated(DataAdapterResult object) {
        Field member = fieldCache.get(object.getKey());
        if (member != null) {
            try {
                member.setAccessible(true);
                member.set(this, object.getResult());
                // invalidate the cache.
                this.result = null;
                this.resultComputed = false;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * This method acts a runtime lookup optimized for performance (to avoid reflection),
     * exposing @EmitData.name() as a runtime attribute.
     *
     * This method is not intended to be overridden by subclasses. Interested parties should annotate their
     * bizlogics with @EmitData and provide a name there.
     */
    @Override
    public final String name() {
        return emittedDataName;
    }

    /**
     *
     * @param tefContext Tef Context
     * @return The emitted object.
     */
    public abstract T adapt(TefContext tefContext) throws TefExecutionException;
}
