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

import flipkart.tef.exception.TefExecutionException;

import java.util.Optional;
/**
 * A bizlogic is a piece of arbitrary logic that can be executed as part of a `SimpleFlow` .
 * Bizlogics can depend upon each other to form an Execution DAG.
 *
 * 
 * Date: 19/06/20
 * Time: 4:24 PM
 */
public interface IBizlogic {
    /**
     * Implement the business logic here.
     *
     * @param tefContext Contains request specific data
     */

    void execute(TefContext tefContext) throws TefExecutionException;

    /**
     * This method will be invoked by the Flow Executor to extract the data to be made available
     * wherever its being injected.
     *
     * @param tefContext Contains request specific data
     * @return Optional.empty if no data is produced or not applicable.
     */
    default Optional<DataAdapterResult> executeForData(TefContext tefContext) throws TefExecutionException {
        execute(tefContext);
        return Optional.empty();
    }

}
