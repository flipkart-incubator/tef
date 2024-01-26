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

import com.google.common.base.Preconditions;

/**
 * This class represents the key that is used to stash the DataAdapter response into the Data Context
 * 
 * Date: 19/01/21
 */
public class DataAdapterKey<T> {

    // The name of the data being emitted. Its allowed for the name to be empty (which is the default behavior) .
    private final String name;
    private final Class<T> resultClass;

    public DataAdapterKey(String name, Class<T> resultClass) {
        Preconditions.checkArgument(name != null);
        this.name = name;
        this.resultClass = resultClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataAdapterKey<?> that = (DataAdapterKey<?>) o;

        if (!getName().equals(that.getName())) return false;
        return resultClass.equals(that.resultClass);
    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + resultClass.hashCode();
        return result;
    }

    public String getName() {
        return name;
    }

    public Class<T> getResultClass() {
        return resultClass;
    }
}
