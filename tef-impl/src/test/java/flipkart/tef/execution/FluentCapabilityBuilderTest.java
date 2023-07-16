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

package flipkart.tef.execution;

import com.google.common.collect.ImmutableList;
import flipkart.tef.bizlogics.BasicEnrichmentBizlogic;
import flipkart.tef.bizlogics.BasicValidationBizlogic;
import flipkart.tef.bizlogics.DataAdapterBizlogic;
import flipkart.tef.bizlogics.IBizlogic;
import flipkart.tef.bizlogics.TefContext;
import flipkart.tef.capability.BizlogicDependency;
import flipkart.tef.capability.CapabilityDefinition;
import flipkart.tef.capability.EmptyCapabilityDefinition;
import flipkart.tef.capability.model.EnrichmentResultData;
import flipkart.tef.capability.model.ValidationResultData;
import flipkart.tef.flow.SimpleFlow;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FluentCapabilityBuilderTest {

    @BeforeClass
    public static void setup() {

    }

    @Test
    public void testNullCapability() throws Exception {
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition() {

            @Override
            public String name() {
                return null;
            }

            @Override
            public List<? extends CapabilityDefinition> dependentCapabilities() {
                return null;
            }

            @Override
            public List<Class<? extends BasicValidationBizlogic>> validators() {
                return null;
            }

            @Override
            public List<Class<? extends BasicEnrichmentBizlogic>> enrichers() {
                return null;
            }

            @Override
            public List<Class<? extends DataAdapterBizlogic>> adapters() {
                return null;
            }

            @Override
            public List<Class<? extends IBizlogic>> bizlogics() {
                return null;
            }

            @Override
            public List<Class<? extends IBizlogic>> exclusions() {
                return null;
            }

            @Override
            public List<BizlogicDependency> bizlogicDependencies() {
                return null;
            }
        };
        FluentCapabilityBuilder manager = new FluentCapabilityBuilder();
        try {
            manager.withCapability(capabilityDefinition).dataflow();
            assertTrue("Exception should have been thrown", true);
        } catch (IllegalArgumentException e) {
            assertEquals(FlowBuilder.Messages.COULD_NOT_DEDUCE_THE_STARTING_STEP, e.getMessage());
        }
    }

    @Test
    public void testEmptyCapability() throws Exception {
        CapabilityDefinition capabilityDefinition = new EmptyCapabilityDefinition() {
            @Override
            public String name() {
                return "c1";
            }
        };

        FluentCapabilityBuilder manager = new FluentCapabilityBuilder();
        try {
            manager.withCapability(capabilityDefinition).dataflow();
            assertTrue("Exception should have been thrown", true);
        } catch (IllegalArgumentException e) {
            assertEquals(FlowBuilder.Messages.COULD_NOT_DEDUCE_THE_STARTING_STEP, e.getMessage());
        }
    }

    @Test
    public void testBasicCapability() throws Exception {

        CapabilityDefinition capabilityDefinition = new EmptyCapabilityDefinition() {
            @Override
            public String name() {
                return "C1";
            }

            @Override
            public List<? extends CapabilityDefinition> dependentCapabilities() {
                return Collections.emptyList();
            }

            @Override
            public List<Class<? extends BasicValidationBizlogic>> validators() {
                return ImmutableList.of(BasicValidationBizlogic1.class);
            }

            @Override
            public List<Class<? extends BasicEnrichmentBizlogic>> enrichers() {
                return ImmutableList.of(BasicEnrichmentBizlogic1.class);
            }

            @Override
            public List<Class<? extends DataAdapterBizlogic>> adapters() {
                return ImmutableList.of(DataAdapterBizlogic1.class);
            }

            @Override
            public List<Class<? extends IBizlogic>> exclusions() {
                return Collections.emptyList();
            }
        };

        FluentCapabilityBuilder manager = new FluentCapabilityBuilder();
        SimpleFlow flow = manager.withCapability(capabilityDefinition).dataflow();

        assertNotNull(flow);

        assertEquals(3, flow.getBizlogics().size());
        assertTrue(flow.getBizlogics().contains(BasicValidationBizlogic1.class));
        assertTrue(flow.getBizlogics().contains(BasicEnrichmentBizlogic1.class));
        assertTrue(flow.getBizlogics().contains(DataAdapterBizlogic1.class));

    }

    class BasicValidationBizlogic1 extends BasicValidationBizlogic {

        @Override
        protected void applyValidationResult(ValidationResultData result, Object o) {

        }

        @Override
        public ValidationResultData validate() {
            return null;
        }

        @Override
        public Object getTarget() {
            return null;
        }
    }

    class BasicEnrichmentBizlogic1 extends BasicEnrichmentBizlogic {

        @Override
        public EnrichmentResultData enrich() {
            return null;
        }

        @Override
        public void map(EnrichmentResultData enriched, Object o) {

        }

        @Override
        public Object getTarget() {
            return null;
        }
    }

    class DataAdapterBizlogic1 extends DataAdapterBizlogic<Object> {

        @Override
        public Object adapt(TefContext tefContext) {
            return null;
        }
    }
}