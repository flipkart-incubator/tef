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

package flipkart.tef;

import com.google.inject.AbstractModule;
import flipkart.tef.execution.DataInjector;
import flipkart.tef.execution.DefaultDataInjector;

/**
 * This class contains guice binding relevant for tef.
 *
 * Date: 01/07/21
 */
public class TefGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DataInjector.class).to(DefaultDataInjector.class);
    }
}
