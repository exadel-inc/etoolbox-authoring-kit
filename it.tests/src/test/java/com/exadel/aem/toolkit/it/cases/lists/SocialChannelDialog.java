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
package com.exadel.aem.toolkit.it.cases.lists;

import com.exadel.aem.toolkit.api.annotations.lists.ListItem;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.it.cases.Constants;

@AemComponent(
    path = Constants.JCR_COMPONENTS_ROOT + "/lists/socialChannel",
    title = "Social Channel Item",
    componentGroup = Constants.GROUP_HIDDEN
)
@Dialog
@ListItem
public class SocialChannelDialog {
    @DialogField(
        label = "ID",
        required = true
    )
    @TextField
    private String id;

    @DialogField(
        label = "Title",
        required = true
    )
    @TextField
    private String title;

    @DialogField(
        label = "URL template",
        required = true
    )
    @TextField
    private String url;

    @DialogField(
        label = "Icon",
        required = true
    )
    @TextField
    private String icon;
}
