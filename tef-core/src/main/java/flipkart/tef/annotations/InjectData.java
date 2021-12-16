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

package flipkart.tef.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to define Data Dependencies in a BizLogic.
 * The Task Executor is going to ensure that the DataBizlogic that can generate the corresponding data
 * is executed before the execution of the current BizLogic.
 *
 * 
 * Date: 19/06/20
 * Time: 4:30 PM
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectData {

    /**
     * If a DataAdapter is injecting dependent data, and if that data can change, the injection
     * can be marked as mutable. In such cases, when the injected data changes,
     * the injectee data adapter will get re-triggered.
     *
     * @return
     */
    boolean mutable() default false;

    /**
     * This flag should be used on the bizlogics which are ok to receive a null value for this injection.
     *
     * @return
     */
    boolean nullable() default false;

    /**
     * Name of the Injected data for specificity
     *
     * @return
     */
    String name() default "";

    /**
     * This parameter is indicative of the fact that the particular injection is optional.
     * No checks will be performed if there are no data adapters present for this injection.
     * Such injections will only be served via the ImplicitBindings.
     *
     * @return
     */
    boolean optional() default false;
}
