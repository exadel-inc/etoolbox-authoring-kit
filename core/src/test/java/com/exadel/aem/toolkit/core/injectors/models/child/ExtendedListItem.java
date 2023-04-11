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
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.api.annotations.injectors.Child;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ExtendedListItem implements SimpleListItem {

    @ValueMapValue(name = JcrConstants.JCR_TITLE)
    private String title;

    @ValueMapValue
    private String value;

    @Child(name = "nested")
    private Nested nestedViaChild;

    @ChildResource(name = "nested")
    private Nested nestedViaChildResource;

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getValue() {
        return value;
    }

    @Nullable
    public Nested getNestedViaChild() {
        return nestedViaChild;
    }

    @Nullable
    public Nested getNestedViaChildResource() {
        return nestedViaChildResource;
    }

    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class Nested {

        @Self
        private Resource resource;

        @Child(name = "../../..")
        private Resource ancestorResource;

        @ValueMapValue
        private String title;

        @ValueMapValue
        private long value;

        @Nullable
        public Resource getAncestorResource() {
            return ancestorResource;
        }

        @Nullable
        public Resource getNestedResource() {
            return resource;
        }

        @Nullable
        public String getNestedTitle() {
            return title;
        }

        public long getNestedValue() {
            return value;
        }
    }
}
