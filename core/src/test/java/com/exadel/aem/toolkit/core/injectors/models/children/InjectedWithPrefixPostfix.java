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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.api.annotations.injectors.Children;
import com.exadel.aem.toolkit.core.injectors.models.child.InjectedWithSelection;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class InjectedWithPrefixPostfix implements ObjectValueHolder {

    @Children(
        name = Constants.CHILDREN_PATH,
        prefix = InjectedWithSelection.PREFIX,
        postfix = InjectedWithSelection.POSTFIX)
    private List<SimpleListItem> value;

    @Children(
        name = Constants.CHILDREN_PATH,
        prefix = InjectedWithSelection.PREFIX,
        postfix = InjectedWithSelection.POSTFIX)
    private Object objectValue;

    private final List<Resource> valueFromConstructor;

    @Inject
    public InjectedWithPrefixPostfix(
        @Children(name = Constants.CHILDREN_PATH, prefix = InjectedWithSelection.PREFIX, postfix = InjectedWithSelection.POSTFIX)
        @Named
        List<Resource> value) {
        valueFromConstructor = value;
    }

    @Nullable
    public List<SimpleListItem> getValue() {
        return value != null
            ? value.stream().filter(item -> StringUtils.isNotEmpty(item.getTitle())).collect(Collectors.toList())
            : Collections.emptyList();
    }

    @Override
    public Object getRawObjectValue() {
        return objectValue;
    }

    public List<Resource> getValueFromConstructor() {
        return valueFromConstructor != null
            ? valueFromConstructor.stream().filter(res -> res.getValueMap().containsKey(JcrConstants.JCR_TITLE)).collect(Collectors.toList())
            : Collections.emptyList();
    }

    @Model(adaptables = {SlingHttpServletRequest.class, Resource.class})
    public interface ValueSupplier {
        @Children(
            name = Constants.CHILDREN_PATH_ABSOLUTE,
            prefix = InjectedWithSelection.PREFIX,
            postfix = InjectedWithSelection.POSTFIX)
        List<SimpleListItem> getValue();
    }
}
