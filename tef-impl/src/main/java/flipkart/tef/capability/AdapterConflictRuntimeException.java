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

import flipkart.tef.bizlogics.IBizlogic;

/**
 * 
 * Date: 02/07/20
 * Time: 2:58 PM
 */
public class AdapterConflictRuntimeException extends RuntimeException {

    private static final String MESSAGE_FORMAT = "Adapter conflict for data %s within %s and %s";

    public AdapterConflictRuntimeException(Class<?> returnType, Class<? extends IBizlogic> b1, Class<? extends IBizlogic> b2) {
        super(String.format(MESSAGE_FORMAT, returnType.getSimpleName(), b1.getSimpleName(), b2.getSimpleName()));
    }
}
