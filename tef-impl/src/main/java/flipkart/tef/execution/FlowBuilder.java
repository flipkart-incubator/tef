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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import flipkart.tef.annotations.DependsOn;
import flipkart.tef.annotations.InjectData;
import flipkart.tef.bizlogics.DataAdapterBizlogic;
import flipkart.tef.bizlogics.DataAdapterKey;
import flipkart.tef.bizlogics.IBizlogic;
import flipkart.tef.bizlogics.IDataBizlogic;
import flipkart.tef.capability.AdapterConflictRuntimeException;
import flipkart.tef.exceptions.UnableToResolveDataFromAdapterRuntimeException;
import flipkart.tef.flow.SimpleFlow;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

// TODO Abstract out Flow Builder Implementation so that it does not have to be exposed to the client

/**
 * Flow Builder takes in the list of bizlogics with any explicit dependencies and exclusions.
 * Any implicit dependencies are discovered and a Execution DAG is generated.
 *
 * 
 * Date: 19/06/20
 * Time: 5:02 PM
 */
public class FlowBuilder {
    private final List<Class<? extends IBizlogic>> bizlogics;
    private final Multimap<Class<? extends IBizlogic>, Class<? extends IBizlogic>> bizlogicDependencyMap;
    private final Multimap<Class<? extends IBizlogic>, Class<? extends IBizlogic>> reverseBizlogicDependencyMap;
    private final Multimap<Class<? extends IBizlogic>, DataAdapterKey<?>> dataDependencyMap;
    private final BiMap<DataAdapterKey<?>, Class<? extends IDataBizlogic<?>>> dataAdapterMap;
    private final Set<DataAdapterKey<?>> implicitDataBindings;
    private final Set<Class<? extends IBizlogic>> excludedBizlogics;

    FlowBuilder() {
        bizlogics = new ArrayList<>();
        bizlogicDependencyMap = ArrayListMultimap.create();
        reverseBizlogicDependencyMap = ArrayListMultimap.create();
        dataDependencyMap = ArrayListMultimap.create();
        dataAdapterMap = HashBiMap.create();
        implicitDataBindings = new HashSet<>();
        excludedBizlogics = new HashSet<>();
    }

    FlowBuilder add(Class<? extends IBizlogic> bizlogic, Class<? extends IBizlogic>... dependencies) {

        if (!bizlogics.contains(bizlogic)) {
            bizlogics.add(bizlogic);
        }

        if (dependencies.length != 0) {
            addDependencies(bizlogic, dependencies);
            // Process the dependencies too
            for (Class<? extends IBizlogic> dependency : dependencies) {
                add(dependency);
            }
        }

        return this;
    }

    @VisibleForTesting
    Collection<Class<? extends IBizlogic>> getBizlogics() {
        return bizlogics;
    }

    @VisibleForTesting
    Multimap<Class<? extends IBizlogic>, Class<? extends IBizlogic>> getBizlogicDependencyMap() {
        return bizlogicDependencyMap;
    }

    @VisibleForTesting
    BiMap<DataAdapterKey<?>, Class<? extends IDataBizlogic<?>>> getDataAdapterMap() {
        return dataAdapterMap;
    }

    @VisibleForTesting
    Multimap<Class<? extends IBizlogic>, DataAdapterKey<?>> getDataDependencyMap() {
        return dataDependencyMap;
    }

    @VisibleForTesting
    Multimap<Class<? extends IBizlogic>, Class<? extends IBizlogic>> getReverseBizlogicDependencyMap() {
        return reverseBizlogicDependencyMap;
    }

    FlowBuilder exclude(Class<? extends IBizlogic> bizlogic) {
        excludedBizlogics.add(bizlogic);
        return this;
    }

    FlowBuilder withImplicitBindings(Class<?>... bindings) {
        for (Class<?> binding : bindings) {
            this.implicitDataBindings.add(new DataAdapterKey<>("", binding));
        }
        return this;
    }

    FlowBuilder withImplicitBindings(DataAdapterKey<?> binding) {
        this.implicitDataBindings.add(binding);
        return this;
    }

    /**
     * Generates the Execution DAG as per the supplied bizlogics.
     *
     * @return
     */
    SimpleFlow build() {

        for (Class<? extends IBizlogic> e : excludedBizlogics) {
            bizlogics.remove(e);
            bizlogicDependencyMap.removeAll(e);
        }

        int idx = 0;
        while (bizlogics.size() > idx) {
            Class<? extends IBizlogic> aClass = bizlogics.get(idx++);
            processBizLogic(aClass);
        }

        List<Class<? extends IBizlogic>> bizlogicsInFlow = new ArrayList<>();
        Stack<Class<? extends IBizlogic>> startNodes = new Stack<>();

        convertDataDependencyToBizLogicDependency();
        for (Class<? extends IBizlogic> bizlogic : bizlogics) {
            if (!bizlogicDependencyMap.containsKey(bizlogic)) {
                startNodes.push(bizlogic);
            }
        }

        Preconditions.checkArgument(startNodes.size() > 0, Messages.COULD_NOT_DEDUCE_THE_STARTING_STEP);

        // Clone for Mutation
        Multimap<Class<? extends IBizlogic>, Class<? extends IBizlogic>> clonedBizlogicDependencyMap = ArrayListMultimap.create(bizlogicDependencyMap);

        while (!startNodes.isEmpty()) {
            Class<? extends IBizlogic> bizlogic = startNodes.pop();
            bizlogicsInFlow.add(bizlogic);

            for (Class<? extends IBizlogic> child : reverseBizlogicDependencyMap.get(bizlogic)) {
                clonedBizlogicDependencyMap.get(child).remove(bizlogic);
                if (clonedBizlogicDependencyMap.get(child).size() == 0) {
                    startNodes.add(child);
                }
            }
        }

        Preconditions.checkArgument(bizlogicsInFlow.size() == bizlogics.size(), Messages.CYCLIC_GRAPHS_ARE_NOT_SUPPORTED);

        return new SimpleFlow(bizlogicsInFlow, dataAdapterMap);
    }

    private void processBizLogic(Class<? extends IBizlogic> bizlogic) {
        handleControlDependency(bizlogic);
        handleDataDependency(bizlogic);
        populateDataAdapterMap(bizlogic);
    }

    private void handleControlDependency(Class<? extends IBizlogic> bizlogic) {
        DependsOn[] dependsOns = bizlogic.getAnnotationsByType(DependsOn.class);
        Preconditions.checkArgument(dependsOns.length <= 1, Messages.MORE_THAN_1_DEPENDS_ON_ANNOTATIONS_FOUND);

        if (dependsOns.length > 0) {
            addDependencies(bizlogic, dependsOns[0].value());
            for (Class<? extends IBizlogic> dependency : dependsOns[0].value()) {
                add(dependency);
            }
        }
    }

    private void convertDataDependencyToBizLogicDependency() {

        for (Map.Entry<Class<? extends IBizlogic>, Collection<DataAdapterKey<?>>> entry : dataDependencyMap.asMap().entrySet()) {
            for (DataAdapterKey<?> injectedData : entry.getValue()) {
                if (implicitDataBindings.contains(injectedData)) {
                    /**
                     * If there are implicit bindings - i.e. data that will be passed as part of the data context
                     * No need to validate that an adapter should be present for it.
                     */
                    continue;
                }
                Class<? extends IDataBizlogic<?>> adapter = dataAdapterMap.get(injectedData);
                Preconditions.checkArgument(adapter != null, String.format(Messages.DATA_ADAPTER_NOT_RESOLVED_FOR,
                        injectedData.getResultClass().getCanonicalName()));
                addDependencies(entry.getKey(), adapter);
            }
        }
    }

    private void populateDataAdapterMap(Class<? extends IBizlogic> bizlogic) {
        try {
            if (DataAdapterBizlogic.class.isAssignableFrom(bizlogic)) {
                Class<? extends DataAdapterBizlogic<?>> dataAdapterBizLogic = (Class<? extends DataAdapterBizlogic<?>>) bizlogic;

                Class<?> returnType = null;

                /*
                 * Using Sun implementation to get generic parameter type. Other considered options were
                 * 1. Have an interface return the class type - This would require us to instantiate the class
                 * 2. Have a Type Parameter via Guava - This would require us to instantiate the class
                 * 3. Get the return type of known interface method ( `adapt` ) - this will not work for cases when
                 *          adapt method is overridden to provide custom context as inputs
                 * 4. Using sun's implementation to get the type param. - Using this approach
                 */
                returnType = getReturnTypeFromBizlogicUsingSunApi(dataAdapterBizLogic, new ArrayList<>());

                DataAdapterKey<?> key = new DataAdapterKey<>(
                        DataAdapterBizlogic.getEmittedDataName(dataAdapterBizLogic), returnType);

                if (!dataAdapterMap.containsKey(key)) {
                    dataAdapterMap.put(key, dataAdapterBizLogic);
                } else {
                    throw new AdapterConflictRuntimeException(returnType, bizlogic, dataAdapterMap.get(key));
                }
            }
        } catch (AdapterConflictRuntimeException|UnableToResolveDataFromAdapterRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<?> getReturnTypeFromBizlogicUsingSunApi(Class<? extends DataAdapterBizlogic<?>> dataAdapterBizLogic, List<Class<? extends DataAdapterBizlogic<?>>> classHierarchy) {
        classHierarchy.add(dataAdapterBizLogic);
        if (dataAdapterBizLogic.getGenericSuperclass() instanceof ParameterizedTypeImpl) {
            ParameterizedTypeImpl genericSuperClass = (ParameterizedTypeImpl) dataAdapterBizLogic.getGenericSuperclass();
            if (genericSuperClass.getActualTypeArguments()[0] instanceof Class) {
                return (Class<?>) genericSuperClass.getActualTypeArguments()[0];
            } else if (genericSuperClass.getActualTypeArguments()[0] instanceof ParameterizedTypeImpl) {
                // The type itself is parameterized
                return ((ParameterizedTypeImpl) genericSuperClass.getActualTypeArguments()[0]).getRawType();
            }
        } else {
            // This could be a case of a data adapter being a subclass of another
            // data adapter where type parameter is specified on the superclass
            if(DataAdapterBizlogic.class.isAssignableFrom(dataAdapterBizLogic.getSuperclass())) {
                return getReturnTypeFromBizlogicUsingSunApi((Class<? extends DataAdapterBizlogic<?>>)dataAdapterBizLogic.getSuperclass(), classHierarchy);
            }
        }

        throw new UnableToResolveDataFromAdapterRuntimeException(classHierarchy);
    }

    static class Messages {
        public static final String CYCLIC_GRAPHS_ARE_NOT_SUPPORTED = "Cyclic Graphs are not supported";
        public static final String A_BIZLOGIC_CANNOT_DEPEND_ON_SELF = "A bizlogic cannot depend on Self";
        public static final String MORE_THAN_1_DEPENDS_ON_ANNOTATIONS_FOUND = "More than 1 @DependsOn annotations found";
        public static final String COULD_NOT_DEDUCE_THE_STARTING_STEP = "Could not deduce the starting step";
        public static final String DATA_ADAPTER_NOT_RESOLVED_FOR = "Data Adapter not resolved for %s";
    }

    private void addDependencies(Class<? extends IBizlogic> bizlogic, Class<? extends IBizlogic>... dependencies) {
        for (Class<? extends IBizlogic> dependency : dependencies) {
            Preconditions.checkArgument(bizlogic != dependency, Messages.A_BIZLOGIC_CANNOT_DEPEND_ON_SELF);
            bizlogicDependencyMap.put(bizlogic, dependency);
            reverseBizlogicDependencyMap.put(dependency, bizlogic);
        }
    }

    private void handleDataDependency(Class<? extends IBizlogic> bizlogic) {
        List<Field> fieldList = new ArrayList<>();

        Class<?> that = bizlogic;
        do {
            Field[] fields = that.getDeclaredFields();
            fieldList.addAll(Arrays.asList(fields));
            that = that.getSuperclass();
        } while (!that.equals(Object.class));

        for (Field field : fieldList) {
            InjectData injectable = field.getAnnotation(InjectData.class);
            if (injectable != null && !injectable.optional()) {
                dataDependencyMap.put(bizlogic, new DataAdapterKey<>(injectable.name(), field.getType()));
            }
        }
    }
}
