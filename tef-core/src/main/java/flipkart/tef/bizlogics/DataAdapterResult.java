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

import com.google.common.base.Preconditions;

/**
 * This class represents the response received from a DataAdapter Bizlogic
 * 
 * Date: 19/01/21
 */
public class DataAdapterResult {
    private final Object result;
    private DataAdapterKey key;

    public DataAdapterResult(Object result) {
        this(result, "", null);
    }

    public DataAdapterResult(Object result, String name, Class<?> resultType) {
        Preconditions.checkArgument(name != null);
        this.result = result;

        // Result could be null in case of nullable data adapters
        // This is to support that
        if (result != null) {
            this.key = new DataAdapterKey(name, resultType == null ? result.getClass() : resultType);
        }
    }

    public Object getResult() {
        return result;
    }

    public DataAdapterKey getKey() {
        return key;
    }
}
