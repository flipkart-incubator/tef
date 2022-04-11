/*
 * Copyright [2021] [The Original Author]
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

package flipkart.tef.exceptions;

import flipkart.tef.bizlogics.DataAdapterBizlogic;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This exception is thrown when Tef cannot figure the return type of data adapter.
 * Date: 29/03/22
 * Time: 8:28 AM
 */
public class UnableToResolveDataFromAdapterRuntimeException extends RuntimeException {

    private static final String MESSAGE_FORMAT = "Unable to resolve data from Data Adapter Class Hierarchy %s";

    public UnableToResolveDataFromAdapterRuntimeException(List<Class<? extends DataAdapterBizlogic<?>>> classHierarchy) {
        super(String.format(MESSAGE_FORMAT,
                classHierarchy.stream().map(Class::getSimpleName)
                        .collect(Collectors.joining(" -> "))
                )
        );
    }
}
