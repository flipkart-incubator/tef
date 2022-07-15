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

import com.google.common.collect.Lists;
import flipkart.tef.bizlogics.BasicEnrichmentBizlogic;
import flipkart.tef.bizlogics.BasicValidationBizlogic;
import flipkart.tef.bizlogics.DataAdapterBizlogic;
import flipkart.tef.bizlogics.IBizlogic;
import flipkart.tef.capability.BizlogicDependency;
import flipkart.tef.capability.CapabilityDefinition;
import flipkart.tef.flow.SimpleFlow;

import java.util.Collection;
import java.util.List;

/**
 * Simple Flow Builder
 * <p>
 * 
 * Date: 23/06/20
 * Time: 6:57 PM
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class FluentCapabilityBuilder {

    private final FlowBuilder flowBuilder;

    public FluentCapabilityBuilder() {
        flowBuilder = new FlowBuilder();
    }

    public FluentCapabilityBuilder withValidator(Class<? extends BasicValidationBizlogic>... validators) {
        return this.withValidators(Lists.newArrayList(validators));
    }

    public FluentCapabilityBuilder withEnricher(Class<? extends BasicEnrichmentBizlogic>... enrichers) {
        return this.withEnrichers(Lists.newArrayList(enrichers));
    }

    public FluentCapabilityBuilder withAdapter(Class<? extends DataAdapterBizlogic> adapter) {
        flowBuilder.add(adapter);
        return this;
    }

    public FluentCapabilityBuilder withCapability(CapabilityDefinition... capabilities) {
        return this.withCapabilities(Lists.newArrayList(capabilities));
    }

    public FluentCapabilityBuilder withValidators(List<Class<? extends BasicValidationBizlogic>> validators) {

        if (isNullOrEmpty(validators)) {
            return this;
        }

        for (Class<? extends BasicValidationBizlogic> validator : validators) {
            flowBuilder.add(validator);
        }
        return this;
    }

    public FluentCapabilityBuilder withEnrichers(List<Class<? extends BasicEnrichmentBizlogic>> enrichers) {

        if (isNullOrEmpty(enrichers)) {
            return this;
        }

        for (Class<? extends BasicEnrichmentBizlogic> enricher : enrichers) {
            flowBuilder.add(enricher);
        }
        return this;
    }

    public FluentCapabilityBuilder withAdapters(List<Class<? extends DataAdapterBizlogic>> adapters) {

        if (isNullOrEmpty(adapters)) {
            return this;
        }

        for (Class<? extends DataAdapterBizlogic> adapter : adapters) {
            flowBuilder.add(adapter);
        }
        return this;
    }

    public FluentCapabilityBuilder withCapabilities(List<? extends CapabilityDefinition> capabilities) {

        if (isNullOrEmpty(capabilities)) {
            return this;
        }

        for (CapabilityDefinition capability : capabilities) {
            this.withEnrichers(capability.enrichers());
            this.withValidators(capability.validators());
            this.withAdapters(capability.adapters());
            this.withBizlogics(capability.bizlogics());

            if (!isNullOrEmpty(capability.exclusions())) {
                capability.exclusions().forEach(this::withExclusion);
            }

            if (!isNullOrEmpty(capability.dependentCapabilities())) {
                // TODO fix possible infinite recursion here
                this.withCapabilities(capability.dependentCapabilities());
            }

            List<BizlogicDependency> bizlogicDependencies = capability.bizlogicDependencies();
            if (!isNullOrEmpty(bizlogicDependencies)) {
                for (BizlogicDependency dependency : bizlogicDependencies) {
                    this.withDependency(dependency.getBizlogic(), dependency.getDependencies());
                }
            }

        }
        return this;
    }

    public FluentCapabilityBuilder withName(String name) {
        return this;
    }

    public SimpleFlow dataflow() {
        return flowBuilder.build();
    }

    public FluentCapabilityBuilder withImplicitBindings(Class<?>... bindings) {
        flowBuilder.withImplicitBindings(bindings);
        return this;
    }

    public FluentCapabilityBuilder withExclusion(Class<? extends IBizlogic> exclusion) {

        if (exclusion == null) {
            return this;
        }

        flowBuilder.exclude(exclusion);
        return this;
    }

    public FluentCapabilityBuilder withDependency(Class<? extends IBizlogic> bizlogic, Class<? extends IBizlogic>[] dependencies) {
        flowBuilder.add(bizlogic, dependencies);
        return this;
    }

    public FluentCapabilityBuilder withBizlogic(Class<? extends IBizlogic> bizlogic) {
        flowBuilder.add(bizlogic);
        return this;
    }

    public FluentCapabilityBuilder withBizlogics(List<Class<? extends IBizlogic>> bizlogics) {

        if (isNullOrEmpty(bizlogics)) {
            return this;
        }

        for (Class<? extends IBizlogic> bizlogic : bizlogics) {
            flowBuilder.add(bizlogic);
        }
        return this;
    }

    private boolean isNullOrEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }
}
