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

package flipkart.tef.capability;

import flipkart.tef.bizlogics.BasicEnrichmentBizlogic;
import flipkart.tef.bizlogics.BasicValidationBizlogic;
import flipkart.tef.bizlogics.DataAdapterBizlogic;
import flipkart.tef.bizlogics.IBizlogic;

import java.util.ArrayList;
import java.util.List;

/**
 * A stub implementation of `CapabilityDefinition`.
 *
 * 
 * Date: 10/06/20
 * Time: 8:30 AM
 */
public abstract class EmptyCapabilityDefinition implements CapabilityDefinition {

    @Override
    public List<? extends CapabilityDefinition> dependentCapabilities() {
        return emptyList();
    }

    @Override
    public List<Class<? extends BasicValidationBizlogic>> validators() {
        return emptyList();
    }

    @Override
    public List<Class<? extends BasicEnrichmentBizlogic>> enrichers() {
        return emptyList();
    }

    @Override
    public List<Class<? extends DataAdapterBizlogic>> adapters() {
        return emptyList();
    }

    @Override
    public List<Class<? extends IBizlogic>> bizlogics() {
        return emptyList();
    }

    @Override
    public List<Class<? extends IBizlogic>> exclusions() {
        return emptyList();
    }

    protected final <T> List<T> emptyList() {
        return new ArrayList<>();
    }

    @Override
    public List<BizlogicDependency> bizlogicDependencies() {
        return emptyList();
    }
}
