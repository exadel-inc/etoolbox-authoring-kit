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
package com.exadel.aem.toolkit.it;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import com.exadel.aem.toolkit.it.base.PackageSuite;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntimeUtil;

/**
 * Shortcut class for running all available test cases in a batch
 */
@RunWith(PackageSuite.class)
@SuiteClasses({
    AllowedChildrenTest.class,
    ListsTest.class,
    OptionProviderTest.class,
    IgnoreFreshnessTest.class
})
@SuppressWarnings("java:S2187") // Tests are run via {@code SuiteClasses(...)}
public class AllTests {
    @BeforeClass
    public static void setUp() {
        PluginRuntimeUtil.doInit();
    }
    @AfterClass
    public static void tearDown() {
        PluginRuntimeUtil.doClose();
    }
}
