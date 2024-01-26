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

package flipkart.tef.capability;

import flipkart.tef.bizlogics.BasicEnrichmentBizlogic;
import flipkart.tef.bizlogics.BasicValidationBizlogic;
import flipkart.tef.bizlogics.DataAdapterBizlogic;
import flipkart.tef.bizlogics.IBizlogic;

import java.util.List;

/**
 * A capability is defined as a set of bizlogics that provide a capability/feature.
 * A Capability would be made up of a set of service interactions/model enrichers/validators
 * and any bizlogic exclusions.
 * A capability can be dependent upon other capabilities, in which case bizlogics
 * in the dependent capability will be also be incorporated.
 *
 * The entire list of bizlogics are then used to create an Execution DAG resolving all the
 * control/data dependencies.
 *
 * 
 * Date: 10/06/20
 * Time: 8:30 AM
 */
public interface CapabilityDefinition {

    /**
     * @return A unique name for this capability.
     */
    String name();

    /**
     * @return A list of dependent capabilities.
     */
    List<? extends CapabilityDefinition> dependentCapabilities();

    /**
     * @return A list of validators that are part of this capability.
     */
    List<Class<? extends BasicValidationBizlogic>> validators();

    /**
     * @return A list of enrichers that are part of this capability.
     */
    List<Class<? extends BasicEnrichmentBizlogic>> enrichers();

    /**
     * @return A list of data adapters that are part of this capability.
     */
    List<Class<? extends DataAdapterBizlogic>> adapters();

    /**
     * @return A list of bizlogics that are part of this capability.
     */
    List<Class<? extends IBizlogic>> bizlogics();

    /**
     * This exclusion is applied after all the dependencies have been crawled transitively.
     *
     * @return List if bizlogics that should be excluded in this capability.
     */
    List<Class<? extends IBizlogic>> exclusions();

    /**
     * @return A list of explicit bizlogic dependencies.
     */
    List<BizlogicDependency> bizlogicDependencies();
}
