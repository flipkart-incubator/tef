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

package flipkart.tef.bizlogics;

import flipkart.tef.capability.model.ValidationResultData;
import flipkart.tef.exception.TefExecutionException;

/**
 * A specialized bizlogic that can be used to validate items.
 * <p>
 * <p>
 * Date: 19/06/20
 * Time: 4:25 PM
 */
public abstract class BasicValidationBizlogic<Target, Key, Value> implements IBizlogic {


    @Override
    public final void execute(TefContext tefContext) throws TefExecutionException {
        applyValidationResult(validate(), getTarget());
    }

    protected abstract void applyValidationResult(ValidationResultData<Key, Value> result, Target target) throws TefExecutionException;

    /**
     * Implement this method to run the validation logic and return the validation status
     *
     *
     * @return Item Level Validation Status
     */
    public abstract ValidationResultData<Key, Value> validate() throws TefExecutionException;

    public abstract Target getTarget();
}
