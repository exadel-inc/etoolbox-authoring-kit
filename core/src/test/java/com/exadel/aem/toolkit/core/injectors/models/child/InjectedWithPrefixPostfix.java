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
package com.exadel.aem.toolkit.core.injectors.models.child;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.exadel.aem.toolkit.api.annotations.injectors.Child;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.TestConstants;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class InjectedWithPrefixPostfix implements InjectedWithSelection {
    private static final String CHILD_PATH = "list/list_item_4";

    @Child(name = CHILD_PATH, prefix = PREFIX, postfix = POSTFIX)
    private SimpleListItem value;

    @Child(name = CHILD_PATH, prefix = PREFIX, postfix = POSTFIX)
    private Object objectValue;

    @Child(name = CHILD_PATH, prefix = PREFIX, postfix = POSTFIX)
    private ExtendedListItem extended;

    @Self
    private PrefixedPostfixedValueSupplier supplier;

    private final Resource valueFromConstructor;

    @Inject
    public InjectedWithPrefixPostfix(@Child(name = CHILD_PATH, prefix = PREFIX, postfix = POSTFIX) @Named Resource value) {
        valueFromConstructor = value;
    }

    @Nullable
    public SimpleListItem getValue() {
        return value;
    }

    @Override
    @Nullable
    public Object getObjectValue() {
        return objectValue;
    }

    public Resource getValueFromConstructor() {
        return valueFromConstructor;
    }

    @Override
    @Nullable
    public ExtendedListItem getExtendedValue() {
        return extended;
    }

    @Nullable
    public ValueSupplier getSupplier() {
        return supplier;
    }

    @Model(adaptables = {SlingHttpServletRequest.class, Resource.class})
    interface PrefixedPostfixedValueSupplier extends ValueSupplier {
        @Child(
            name = TestConstants.ROOT_PAGE_CONTENT + CoreConstants.SEPARATOR_SLASH + CHILD_PATH,
            prefix = PREFIX,
            postfix = POSTFIX)
        SimpleListItem getValue();
    }
}
