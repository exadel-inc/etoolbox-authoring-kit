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
package com.exadel.aem.toolkit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.exadel.aem.toolkit.api.annotations.meta.StringTransformationTest;
import com.exadel.aem.toolkit.core.configurator.models.internal.ConfigDefinitionTest;
import com.exadel.aem.toolkit.core.configurator.models.internal.RenderConditionTest;
import com.exadel.aem.toolkit.core.configurator.services.ConfigChangeListenerTest;
import com.exadel.aem.toolkit.core.configurator.services.ConfigDataUtilTest;
import com.exadel.aem.toolkit.core.configurator.servlets.form.ConfigDataSourceTest;
import com.exadel.aem.toolkit.core.configurator.servlets.form.FieldUtilTest;
import com.exadel.aem.toolkit.core.configurator.servlets.form.ValueUtilTest;
import com.exadel.aem.toolkit.core.configurator.servlets.replication.ReplicationServletTest;
import com.exadel.aem.toolkit.core.configurator.utils.PermissionUtilTest;
import com.exadel.aem.toolkit.core.injectors.ChildInjectorTest;
import com.exadel.aem.toolkit.core.injectors.ChildrenInjectorTest;
import com.exadel.aem.toolkit.core.injectors.EToolboxListInjectorTest;
import com.exadel.aem.toolkit.core.injectors.EnumValueInjectorTest;
import com.exadel.aem.toolkit.core.injectors.I18nInjectorTest;
import com.exadel.aem.toolkit.core.injectors.RequestAttributeInjectorTest;
import com.exadel.aem.toolkit.core.injectors.RequestParamInjectorTest;
import com.exadel.aem.toolkit.core.injectors.RequestSelectorsInjectorTest;
import com.exadel.aem.toolkit.core.injectors.RequestSuffixInjectorTest;
import com.exadel.aem.toolkit.core.injectors.utils.FilteredResourceDecoratorTest;
import com.exadel.aem.toolkit.core.lists.models.ListItemTest;
import com.exadel.aem.toolkit.core.lists.servlets.ItemComponentsServletTest;
import com.exadel.aem.toolkit.core.lists.servlets.ListsServletTest;
import com.exadel.aem.toolkit.core.lists.utils.ListHelperTest;
import com.exadel.aem.toolkit.core.lists.utils.ListPageUtilTest;
import com.exadel.aem.toolkit.core.lists.utils.ListResourceUtilTest;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.resolvers.OptionProviderConstantsTest;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.resolvers.OptionProviderEnumsTest;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.resolvers.OptionProviderHttpTest;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.resolvers.OptionProviderInlineOptionsTest;
import com.exadel.aem.toolkit.core.optionprovider.services.impl.resolvers.OptionProviderTest;
import com.exadel.aem.toolkit.core.policymanagement.filters.TopLevelPolicyFilterTest;

/**
 * Shortcut class for running all available test cases in a batch
 */
@RunWith(Suite.class)
@SuiteClasses({
    StringTransformationTest.class,

    ListHelperTest.class,
    ListPageUtilTest.class,
    ListResourceUtilTest.class,

    ListsServletTest.class,
    ListItemTest.class,
    ItemComponentsServletTest.class,

    RequestAttributeInjectorTest.class,
    RequestParamInjectorTest.class,
    RequestSelectorsInjectorTest.class,
    RequestSuffixInjectorTest.class,
    I18nInjectorTest.class,
    EToolboxListInjectorTest.class,
    ChildInjectorTest.class,
    ChildrenInjectorTest.class,
    EnumValueInjectorTest.class,
    FilteredResourceDecoratorTest.class,

    OptionProviderTest.class,
    OptionProviderHttpTest.class,
    OptionProviderEnumsTest.class,
    OptionProviderConstantsTest.class,
    OptionProviderInlineOptionsTest.class,

    ConfigChangeListenerTest.class,
    ConfigDataSourceTest.class,
    ConfigDataUtilTest.class,
    ConfigDefinitionTest.class,
    ReplicationServletTest.class,
    RenderConditionTest.class,
    FieldUtilTest.class,
    PermissionUtilTest.class,
    ValueUtilTest.class,

    TopLevelPolicyFilterTest.class
})
public class AllTests {
}
