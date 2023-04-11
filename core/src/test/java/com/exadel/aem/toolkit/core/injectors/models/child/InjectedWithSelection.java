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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;

import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

public interface InjectedWithSelection {

    String PREFIX = "prefix_";
    String POSTFIX = "_postfix";

    @Nullable
    SimpleListItem getValue();

    Resource getValueFromConstructor();

    @Nullable
    ExtendedListItem getExtendedValue();

    @Nullable
    ValueSupplier getSupplier();

    @Model(adaptables = {SlingHttpServletRequest.class, Resource.class})
    interface ValueSupplier {
        SimpleListItem getValue();
    }
}
