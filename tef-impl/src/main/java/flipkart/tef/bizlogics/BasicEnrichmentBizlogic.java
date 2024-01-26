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

package flipkart.tef.bizlogics;

import flipkart.tef.capability.model.EnrichmentResultData;
import flipkart.tef.exception.TefExecutionException;

/**
 * A specialized bizlogic that is responsible for emitting data
 * that is used to enrich the target as per the mapping rules.
 *
 * 
 * Date: 07/07/20
 * Time: 1:48 PM
 */
public abstract class BasicEnrichmentBizlogic<Key, Value, Target> implements IBizlogic {

    public final void execute(TefContext tefContext) throws TefExecutionException {

        EnrichmentResultData<Key, Value> result = enrich();
        // TODO BP Have namespaces during mapping
        map(result, getTarget());
    }

    /**
     * @return Return an enriched object
     */
    public abstract EnrichmentResultData<Key, Value> enrich() throws TefExecutionException;

    /**
     * This method is supposed to take the enriched data and put it at an appropriate place
     * in the client model.
     *
     * @param enriched The enriched object returned by the `enrich` method
     * @param target   The target object on which the enriched data will be applied.
     */
    public abstract void map(EnrichmentResultData<Key, Value> enriched, Target target) throws TefExecutionException;

    /**
     * @return The target on which the data will be enriched.
     */
    public abstract Target getTarget();
}
