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

import flipkart.tef.exception.TefExecutionException;

import java.util.Optional;

/**
 * IDataBizlogic is a flavour of bizlogic which should return a Data Object.
 * These bizlogics aid in Data Dependency.
 *
 * 
 * Date: 11/08/20
 * Time: 2:32 PM
 */

public interface IDataBizlogic<T> extends IBizlogic {

    /**
     * This method is unsupported for IDataBizlogic.
     *
     * @param tefContext
     */
    @Override
    default void execute(TefContext tefContext) {
        throw new UnsupportedOperationException("IDataBizlogic should not invoke void execute");
    }

    /**
     * This method will be invoked by the Flow Executor to extract the data to be made available
     * wherever its being injected.
     *
     * @param tefContext
     * @return
     */
    Optional<DataAdapterResult> executeForData(TefContext tefContext) throws TefExecutionException;

    String name();

}
