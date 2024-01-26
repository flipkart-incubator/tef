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

package flipkart.tef.flow;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import flipkart.tef.bizlogics.DataAdapterKey;
import flipkart.tef.bizlogics.IBizlogic;
import flipkart.tef.bizlogics.IDataBizlogic;

import java.util.List;

/**
 * Simple Flow Represents an Execution DAG. The list of bizlogics are in strict order of execution.
 * Cycles are guaranteed to be not present.
 *
 * 
 * Date: 19/06/20
 * Time: 5:02 PM
 */
public class SimpleFlow {
    /**
     * A strictly ordered list of bizlogics for execution.
     */
    private final List<Class<? extends IBizlogic>> bizlogics;

    /**
     * Map keyed by the Data Object against the Data Adapter that is responsible for producing it.
     */
    private final BiMap<DataAdapterKey<?>, Class<? extends IDataBizlogic<?>>> dataAdapterMap;

    public SimpleFlow(List<Class<? extends IBizlogic>> bizlogics,
                      BiMap<DataAdapterKey<?>, Class<? extends IDataBizlogic<?>>> dataAdapterMap) {
        this.bizlogics = ImmutableList.copyOf(bizlogics);
        this.dataAdapterMap = ImmutableBiMap.copyOf(dataAdapterMap);
    }

    public List<Class<? extends IBizlogic>> getBizlogics() {
        return bizlogics;
    }

    public BiMap<DataAdapterKey<?>, Class<? extends IDataBizlogic<?>>> getDataAdapterMap() {
        return dataAdapterMap;
    }
}
