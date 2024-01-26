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

import flipkart.tef.interfaces.BizlogicInstanceProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This class is used for Maintaining/passing request scoped context information.
 *
 * Date: 14/12/21
 */
public class TefContext {

    // This map is to be used by clients to stash request level context info.
    // This will be made available to bizlogics
    private final Map<String, Object> extensions;
    private final BizlogicInstanceProvider injector;
    private final Consumer<Throwable> exceptionLogger;


    public TefContext(Map<String, Object> additionalContext, BizlogicInstanceProvider injector, Consumer<Throwable> exceptionLogger) {
        this.extensions = new HashMap<>(additionalContext);
        this.injector = injector;
        this.exceptionLogger = exceptionLogger;
    }

    public <T> T getAdditionalContext(String key, Class<T> type) {
        return type.cast(extensions.get(key));
    }

    public BizlogicInstanceProvider getInjector() {
        return injector;
    }

    public Consumer<Throwable> getExceptionLogger() {
        return exceptionLogger;
    }
}
