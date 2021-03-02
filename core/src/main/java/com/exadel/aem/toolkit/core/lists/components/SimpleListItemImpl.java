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

package com.exadel.aem.toolkit.core.lists.components;

import com.day.cq.commons.jcr.JcrConstants;

import com.exadel.aem.toolkit.api.annotations.lists.ListItem;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.lists.models.SimpleListItem;

/**
 * Represents the basic list item which consists of "jct:title" and "value" fields
 */
@AemComponent(
    path = "content/simpleListItem",
    title = "Simple List Item"
)
@Dialog
@ListItem
public class SimpleListItemImpl implements SimpleListItem {

    @DialogField(
        name = JcrConstants.JCR_TITLE,
        label = "Title",
        description = "Provide item title.")
    @TextField
    private String title;

    @DialogField(
        label = "Value",
        description = "Provide item value.")
    @TextField
    private String value;

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }
}
