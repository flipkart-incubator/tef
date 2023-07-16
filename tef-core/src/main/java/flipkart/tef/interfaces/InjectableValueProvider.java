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

package flipkart.tef.interfaces;

import flipkart.tef.exception.TefExecutionException;

/**
 * This interface is used for providing the value that has to be injected in a bizlogic
 * 
 * Date: 16/04/21
 */
public interface InjectableValueProvider {

    /**
     * Returns
     *
     * @param fieldType
     * @param name
     * @return
     */
    Object getValueToInject(Class<?> fieldType, String name) throws TefExecutionException;
}
