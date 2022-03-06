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
package com.exadel.aem.toolkit.core;

import com.exadel.aem.toolkit.core.injectors.ChildInjectorTest;

import com.exadel.aem.toolkit.core.injectors.ChildrenInjectorTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.exadel.aem.toolkit.core.injectors.RequestParamInjectorTest;
import com.exadel.aem.toolkit.core.injectors.RequestSelectorsInjectorTest;
import com.exadel.aem.toolkit.core.injectors.RequestSuffixInjectorTest;
import com.exadel.aem.toolkit.core.lists.models.ListItemTest;
import com.exadel.aem.toolkit.core.lists.servlets.ItemComponentsServletTest;
import com.exadel.aem.toolkit.core.lists.servlets.ListsServletTest;
import com.exadel.aem.toolkit.core.lists.utils.ListHelperTest;
import com.exadel.aem.toolkit.core.lists.utils.ListPageUtilTest;
import com.exadel.aem.toolkit.core.lists.utils.ListResourceUtilTest;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.OptionProviderTest;

/**
 * Shortcut class for running all available test cases in a batch
 */
@RunWith(Suite.class)
@SuiteClasses({
    ListHelperTest.class,
    ListPageUtilTest.class,
    ListResourceUtilTest.class,

    ListsServletTest.class,
    ListItemTest.class,
    ItemComponentsServletTest.class,

    RequestParamInjectorTest.class,
    RequestSelectorsInjectorTest.class,
    RequestSuffixInjectorTest.class,
    ChildInjectorTest.class,
    ChildrenInjectorTest.class,

    OptionProviderTest.class
})
public class AllTests {
}
