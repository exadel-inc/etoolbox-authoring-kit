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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.exadel.aem.toolkit.plugin.handlers.common.ComponentsTest;
import com.exadel.aem.toolkit.plugin.handlers.common.PoliciesTest;
import com.exadel.aem.toolkit.plugin.handlers.common.WriteModeTest;
import com.exadel.aem.toolkit.plugin.handlers.dependson.DependsOnTest;
import com.exadel.aem.toolkit.plugin.handlers.placement.CoincidingNamesTest;
import com.exadel.aem.toolkit.plugin.handlers.placement.IgnoreTest;
import com.exadel.aem.toolkit.plugin.handlers.placement.InheritanceTest;
import com.exadel.aem.toolkit.plugin.handlers.placement.OrderingTest;
import com.exadel.aem.toolkit.plugin.handlers.placement.ReplacementTest;
import com.exadel.aem.toolkit.plugin.handlers.placement.layouts.LayoutTest;
import com.exadel.aem.toolkit.plugin.handlers.widgets.WidgetsTest;
import com.exadel.aem.toolkit.plugin.maven.ExceptionsTest;
import com.exadel.aem.toolkit.plugin.maven.ValidationsTest;
import com.exadel.aem.toolkit.plugin.targets.TargetsTest;
import com.exadel.aem.toolkit.plugin.utils.AnnotationUtilTest;
import com.exadel.aem.toolkit.plugin.utils.ordering.TopologicalSorterTest;

/**
 * Shortcut class for running all available test cases in a batch
 */
@RunWith(Suite.class)
@SuiteClasses({
    AnnotationUtilTest.class,
    TargetsTest.class,

    ComponentsTest.class,
    WriteModeTest.class,
    WidgetsTest.class,
    PoliciesTest.class,
    DependsOnTest.class,

    LayoutTest.class,
    ReplacementTest.class,
    IgnoreTest.class,
    InheritanceTest.class,
    CoincidingNamesTest.class,
    OrderingTest.class,
    TopologicalSorterTest.class,

    ValidationsTest.class,
    ExceptionsTest.class,
})
public class AllTests {
}