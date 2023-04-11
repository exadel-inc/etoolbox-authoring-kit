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

import java.util.List;
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
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class InjectedByPath {

    private static final String DEFAULT_CHILD_PATH = "defaultChild";

    @Child(name = "list/list_item_1")
    private SimpleListItem value;

    @Child(name = DEFAULT_CHILD_PATH)
    private List<SimpleListItem> singularCollection;

    @Child(name = DEFAULT_CHILD_PATH)
    private SimpleListItem[] singularArray;

    @Child(name = CoreConstants.RELATIVE_PATH_PREFIX)
    private Resource selfResource;

    @Child(name = CoreConstants.PARENT_PATH)
    private Resource parentResource;

    @Self
    private ValueSupplier supplier;

    private final Resource valueFromConstructor;

    @Inject
    public InjectedByPath(@Child @Named("../jcr:content/list/list_item_1") Resource value) {
        valueFromConstructor = value;
    }

    @Nullable
    public SimpleListItem getValue() {
        return value;
    }

    @Nullable
    public List<SimpleListItem> getSingularCollectionValue() {
        return singularCollection;
    }

    @Nullable
    public SimpleListItem[] getSingularArrayValue() {
        return singularArray;
    }

    public Resource getValueFromConstructor() {
        return valueFromConstructor;
    }

    @Nullable
    public Resource getSelfResource() {
        return selfResource;
    }

    @Nullable
    public Resource getParentResource() {
        return parentResource;
    }

    @Nullable
    public ValueSupplier getSupplier() {
        return supplier;
    }

    @Model(adaptables = {SlingHttpServletRequest.class, Resource.class})
    public interface ValueSupplier {

        @Child(name = "/content/jcr:content/list/list_item_1")
        SimpleListItem getValue();
    }
}
