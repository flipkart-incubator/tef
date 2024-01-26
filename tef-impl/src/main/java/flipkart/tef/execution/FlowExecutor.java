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

package flipkart.tef.execution;

import com.google.common.base.Preconditions;
import flipkart.tef.FlowExecutionListener;
import flipkart.tef.bizlogics.DataAdapterKey;
import flipkart.tef.bizlogics.DataAdapterResult;
import flipkart.tef.bizlogics.IBizlogic;
import flipkart.tef.bizlogics.IDataBizlogic;
import flipkart.tef.bizlogics.TefContext;
import flipkart.tef.exception.TefExecutionException;
import flipkart.tef.flow.SimpleFlow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The flow executor takes a SimpleFlow and DataContext as an input and then executes it.
 * The executor takes care of invoking the right lifecycle methods,
 * manages data injection and mutation handlers.
 * <p>
 * This is a stateful class, and a new object should be created for every api call.
 * <p>
 * 
 * Date: 22/06/20
 * Time: 10:03 AM
 */
public class FlowExecutor implements MutationListener, InjectableValueProvider {

    private final SimpleFlow flow;
    private final DataContext context;
    private final AllFlowExecutionListener listener;
    private final Map<DataAdapterKey, IDataBizlogic<?>> dataAdapterInstanceMap;
    private final List<MutationListener> mutationListeners;
    private final TefContext tefContext;

    private final DataInjector dataInjector;


    /**
     * Create an instance of FlowExecutor.
     *  @param flow
     * @param context
     * @param tefContext
     */
    public FlowExecutor(SimpleFlow flow, DataContext context,
                        TefContext tefContext) {
        this.flow = flow;
        this.context = context;
        this.tefContext = tefContext;
        this.dataAdapterInstanceMap = new HashMap<>();
        this.listener = new AllFlowExecutionListener(tefContext);
        this.mutationListeners = new ArrayList<>();
        this.context.addMutationListener(this);

        this.dataInjector = tefContext.getInjector().getInstance(DataInjector.class);
    }

    /**
     * Add a Flow Execution Listener
     *
     * @param listener
     */
    public void addListener(FlowExecutionListener listener) {
        this.listener.addListener(listener);
    }

    /**
     * Remove a Flow Execution Listener
     *
     * @param listener
     */
    public void removeListener(FlowExecutionListener listener) {
        this.listener.removeListener(listener);
    }

    public void execute() throws IllegalAccessException, InstantiationException, DataDependencyException, TefExecutionException {
        Preconditions.checkArgument(flow != null);
        Preconditions.checkArgument(context != null);
        Preconditions.checkArgument(tefContext != null);

        for (Class<? extends IBizlogic> bizlogicClass : flow.getBizlogics()) {
            IBizlogic bizlogic = tefContext.getInjector().getInstance(bizlogicClass);

            if (bizlogic instanceof IDataBizlogic) {
                dataAdapterInstanceMap.put(flow.getDataAdapterMap().inverse().get(bizlogicClass), (IDataBizlogic<?>) bizlogic);
            }

            if (bizlogic instanceof MutationListener) {
                mutationListeners.add((MutationListener) bizlogic);
            }

            listener.pre(bizlogic);
            dataInjector.injectData(bizlogic, bizlogic.getClass(), this);
            try {
                Optional<DataAdapterResult> resultFromBizlogic = bizlogic.executeForData(tefContext);
                resultFromBizlogic.ifPresent(context::put);
            } catch (TefExecutionException e) {
                tefContext.getExceptionLogger().accept(e);
                throw e;
            }
            listener.post(bizlogic);
        }
    }

    @Override
    public Object getValueToInject(Class<?> fieldType, String name) throws TefExecutionException {
        // This step will stash the result in the context
        DataAdapterKey<?> key = new DataAdapterKey<>(name, fieldType);
        IDataBizlogic<?> adapter = dataAdapterInstanceMap.get(key);
        if (adapter != null) {
            // Adapter can be null in case of implicit bindings
            listener.pre(adapter);
            try {
                Optional<DataAdapterResult> adaptedData = adapter.executeForData(tefContext);
                adaptedData.ifPresent(context::put);
            } catch (TefExecutionException e) {
                tefContext.getExceptionLogger().accept(e);
                throw e;
            }
            listener.post(adapter);
        }

        return context.get(key);
    }

    @Override
    public void mutated(DataAdapterResult object) {
        mutationListeners.forEach(i -> i.mutated(object));
    }

    private static class AllFlowExecutionListener implements FlowExecutionListener {
        private final List<FlowExecutionListener> listeners;
        private final TefContext tefContext;

        public AllFlowExecutionListener(TefContext tefContext) {
            this.listeners = new ArrayList<>();
            this.tefContext = tefContext;
        }

        public void addListener(FlowExecutionListener listener) {
            listeners.add(listener);
        }

        public void removeListener(FlowExecutionListener listener) {
            listeners.remove(listener);
        }

        @Override
        public void pre(IBizlogic bizlogic) {
            for (FlowExecutionListener listener : listeners) {
                try {
                    listener.pre(bizlogic);
                } catch (Exception e) {
                    tefContext.getExceptionLogger().accept(e);
                }
            }
        }

        @Override
        public void post(IBizlogic bizlogic) {
            for (FlowExecutionListener listener : listeners) {
                try {
                    listener.post(bizlogic);
                } catch (Exception e) {
                    tefContext.getExceptionLogger().accept(e);
                }
            }
        }
    }
}
