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

package flipkart.tef.interfaces;

/**
 * This interface is intended to expose methods that can help the flow executor eliminate its
 * dependency on concrete implementations of DI (like google guice).
 * <p>
 * Flow Executor needs instances of Bizlogic during execution and this interface is queried for those instances.
 * Clients can stub this with the `Injector` in guice or a similar implementation
 * <p>
 * Date: 16/07/23
 */
public interface BizlogicInstanceProvider {

    <T> T getInstance(Class<T> var1);
}
