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
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.api.annotations.injectors.Children;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.injectors.models.child.ExtendedListItem;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class InjectedByLoopbackPath {

    @Children(name = CoreConstants.RELATIVE_PATH_PREFIX)
    private List<ExtendedListItem> value;

    private final Collection<Resource> valueFromConstructor;

    @Inject
    public InjectedByLoopbackPath(@Children @Named(CoreConstants.RELATIVE_PATH_PREFIX) Collection<Resource> value) {
        valueFromConstructor = value;
    }

    @Nullable
    public List<ExtendedListItem> getValue() {
        return value;
    }

    public Collection<Resource> getValueFromConstructor() {
        return valueFromConstructor;
    }
}
