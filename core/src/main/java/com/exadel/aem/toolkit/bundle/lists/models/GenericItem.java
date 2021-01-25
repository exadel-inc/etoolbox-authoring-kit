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

package com.exadel.aem.toolkit.bundle.lists.models;

import java.util.Objects;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import com.day.cq.commons.jcr.JcrConstants;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.ListItem;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

/**
 * A model that represents the simplest List item which contains of "jct:title" and "value" fields
 */
@Dialog(
    name = "content/genericItem",
    title = "Generic List Item",
    tabs = {
        @Tab(title = "Main Config")
    }
)
@ListItem
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class GenericItem {

    @ValueMapValue(name = JcrConstants.JCR_TITLE)
    @DialogField(
        name = JcrConstants.JCR_TITLE,
        label = "Title",
        description = "Provide Item Title.",
        required = true)
    @TextField
    private String title;

    @ValueMapValue
    @DialogField(
        label = "Value",
        description = "Provide Item Value.",
        required = true)
    @TextField
    private String value;

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericItem that = (GenericItem) o;
        return Objects.equals(title, that.title) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, value);
    }
}
