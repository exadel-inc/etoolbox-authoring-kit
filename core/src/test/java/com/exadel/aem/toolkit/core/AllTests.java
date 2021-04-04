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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.exadel.aem.toolkit.core.lists.models.ListItemTest;
import com.exadel.aem.toolkit.core.lists.services.ListHelperServiceTest;
import com.exadel.aem.toolkit.core.lists.servlets.ItemComponentsServletTest;
import com.exadel.aem.toolkit.core.lists.servlets.ListsServletTest;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.OptionProviderTest;

/**
 * Shortcut class for running all available test cases in a batch
 */
@RunWith(Suite.class)
@SuiteClasses({
    ListItemTest.class,
    ListHelperServiceTest.class,
    ItemComponentsServletTest.class,
    ListsServletTest.class,
    OptionProviderTest.class
})
public class AllTests {
}