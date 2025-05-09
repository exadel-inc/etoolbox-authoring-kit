/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
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
package com.exadel.aem.toolkit.plugin;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.exadel.aem.toolkit.plugin.exceptions.TerminateOnTest;
import com.exadel.aem.toolkit.plugin.handlers.common.AllowedChildrenTest;
import com.exadel.aem.toolkit.plugin.handlers.common.ComponentsTest;
import com.exadel.aem.toolkit.plugin.handlers.common.EditConfigTest;
import com.exadel.aem.toolkit.plugin.handlers.common.IgnoreFreshnessTest;
import com.exadel.aem.toolkit.plugin.handlers.common.MaxChildrenTest;
import com.exadel.aem.toolkit.plugin.handlers.common.WriteModeTest;
import com.exadel.aem.toolkit.plugin.handlers.dependson.DependsOnTest;
import com.exadel.aem.toolkit.plugin.handlers.placement.CoincidenceTest;
import com.exadel.aem.toolkit.plugin.handlers.placement.IgnoreTest;
import com.exadel.aem.toolkit.plugin.handlers.placement.InheritanceTest;
import com.exadel.aem.toolkit.plugin.handlers.placement.OrderingTest;
import com.exadel.aem.toolkit.plugin.handlers.placement.ReplacementTest;
import com.exadel.aem.toolkit.plugin.handlers.placement.layouts.LayoutTest;
import com.exadel.aem.toolkit.plugin.handlers.widgets.WidgetsTest;
import com.exadel.aem.toolkit.plugin.handlers.widgets.common.WidgetsMetaTest;
import com.exadel.aem.toolkit.plugin.maven.PluginContextRule;
import com.exadel.aem.toolkit.plugin.metadata.MetadataTest;
import com.exadel.aem.toolkit.plugin.metadata.RenderingFilterTest;
import com.exadel.aem.toolkit.plugin.metadata.scripting.ScriptingHelperTest;
import com.exadel.aem.toolkit.plugin.sources.SourcesTest;
import com.exadel.aem.toolkit.plugin.targets.TargetsTest;
import com.exadel.aem.toolkit.plugin.utils.XmlMergeHelperTest;
import com.exadel.aem.toolkit.plugin.utils.ordering.TopologicalSorterTest;
import com.exadel.aem.toolkit.plugin.validators.ValidatorsTest;
import com.exadel.aem.toolkit.plugin.writers.PackageInfoTest;

/**
 * Shortcut class for running all available test cases in a batch
 */
@RunWith(Suite.class)
@SuiteClasses({
    SourcesTest.class,
    TargetsTest.class,
    RenderingFilterTest.class,
    ScriptingHelperTest.class,
    XmlMergeHelperTest.class,
    TerminateOnTest.class,

    ComponentsTest.class,
    EditConfigTest.class,
    WriteModeTest.class,
    WidgetsTest.class,
    WidgetsMetaTest.class,
    AllowedChildrenTest.class,
    DependsOnTest.class,
    IgnoreFreshnessTest.class,
    MaxChildrenTest.class,

    LayoutTest.class,
    ReplacementTest.class,
    IgnoreTest.class,
    InheritanceTest.class,
    CoincidenceTest.class,
    OrderingTest.class,
    TopologicalSorterTest.class,

    ValidatorsTest.class,
    MetadataTest.class,
    PackageInfoTest.class,
})
public class AllTests {
    @BeforeClass
    public static void setUp() {
        PluginContextRule.initializeContext();
    }
    @AfterClass
    public static void tearDown() {
        PluginContextRule.closeContext();
    }
}
