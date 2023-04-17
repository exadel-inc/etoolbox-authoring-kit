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
package com.exadel.aem.toolkit.core.injectors.models.children;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.exadel.aem.toolkit.api.annotations.injectors.Children;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class InjectedByPath {

    @Children(name = Constants.CHILDREN_PATH)
    private Collection<SimpleListItem> value;

    @Children(name = Constants.CHILDREN_PATH)
    private Object objectValue;

    @Children(name = Constants.CHILDREN_NAME)
    private SimpleListItem[] arrayValue;

    @Children(name = Constants.CHILDREN_NAME)
    private SimpleListItem singularItem;

    @Children(name = Constants.CHILDREN_NAME)
    private Resource singularResource;

    @Self
    private ValueSupplier supplier;

    private final Set<Resource> valueFromConstructor;

    @Inject
    public InjectedByPath(@Children @Named("../jcr:content/list") Set<Resource> value) {
        valueFromConstructor = value;
    }

    @Nullable
    public Collection<SimpleListItem> getValue() {
        return value;
    }

    @Nullable
    public Object getObjectValue() {
        return objectValue;
    }

    @Nullable
    public SimpleListItem[] getArrayValue() {
        return arrayValue;
    }

    @Nullable
    public SimpleListItem getSingularItem() {
        return singularItem;
    }

    @Nullable
    public Resource getSingularResource() {
        return singularResource;
    }

    public Set<Resource> getValueFromConstructor() {
        return valueFromConstructor;
    }

    @Nullable
    public ValueSupplier getSupplier() {
        return supplier;
    }

    @Model(adaptables = {SlingHttpServletRequest.class, Resource.class})
    public interface ValueSupplier {

        @Children(name = Constants.CHILDREN_PATH_ABSOLUTE)
        List<SimpleListItem> getValue();
    }
}
