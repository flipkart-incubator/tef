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

import flipkart.tef.exception.TefExecutionException;

/**
 * This interface is defines a method for injecting data
 * into bizlogics which have the @InjectData annotation.
 * 
 * Date: 16/04/21
 */
public interface DataInjector {

    /**
     * This method sets the values on all @InjectData annotation member variables.
     *
     * @param bizlogic      Bizlogic instance on which the data has to be injected
     * @param bizlogicClass The class of bizlogic
     * @param valueProvider The value provider which is queried to get the value that will be injected.
     * @throws DataDependencyException
     * @throws IllegalAccessException
     */
    void injectData(Object bizlogic, Class<?> bizlogicClass, InjectableValueProvider valueProvider)
            throws DataDependencyException, IllegalAccessException, TefExecutionException;
}
