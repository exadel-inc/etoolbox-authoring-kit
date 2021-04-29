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
package com.exadel.aem.toolkit.core.lists.models;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.lists.ListItem;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

/**
 * Represents basic EToolbox List item which consists of {@code jcr:title} and {@code value} fields
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@AemComponent(
    path = "content/simpleListItem",
    title = "Simple List Item"
)
@Dialog
@ListItem
public interface SimpleListItem {

    /**
     * Gets the {@code title} part of this item
     * @return String value (non-null)
     */
    @ValueMapValue(name = JcrConstants.JCR_TITLE)
    @Default(values = StringUtils.EMPTY)
    @DialogField(
        name = JcrConstants.JCR_TITLE,
        label = "Title",
        description = "Provide item title.")
    @TextField
    @Place(before = @ClassMember("getValue"))
    String getTitle();

    /**
     * Gets the {@code value} part of this item
     * @return String value (non-null)
     */
    @ValueMapValue
    @Default(values = StringUtils.EMPTY)
    @DialogField(
        label = "Value",
        description = "Provide item value.")
    @TextField
    String getValue();
}
