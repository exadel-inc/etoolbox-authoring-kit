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

package com.exadel.aem.toolkit.test.widget;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.DataSource;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.common.StatusVariantConstants;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;

import static com.exadel.aem.toolkit.core.util.TestConstants.DEFAULT_COMPONENT_NAME;

@Dialog(
        name = DEFAULT_COMPONENT_NAME,
        title = "Select Widget Dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@SuppressWarnings("unused")
public class SelectWidget {
    @DialogField(label = "Rating")
    @Select(options = {
            @Option(text = "Empty", value = ""),
            @Option(text = "Blank", value = " "),
            @Option(
                    text = "1 star",
                    value = "1",
                    selected = true,
                    statusIcon = "/content/dam/samples/icons/1-star-rating.png",
                    statusText = "This is to set 1-star rating",
                    statusVariant = StatusVariantConstants.SUCCESS),
            @Option(text = "2 stars", value = "2"),
            @Option(text = "3 stars", value = "3"),
            @Option(text = "4 stars", value = "4", disabled = true),
            @Option(text = "5 stars", value = "5", disabled = true)
    },
            emptyText = "Select rating",
            multiple = true,
            variant = "default"
    )
    String rating;

    @DialogField(label = "Timezone")
    @Select(options = {
            @Option(text = "UTC +2", value = "+02:00"),
            @Option(text = "UTC +1", value = "+01:00"),
            @Option(text = "UTC", value = "00:00"),
            @Option(text = "UTC -1", value = "-01:00"),
            @Option(text = "UTC -2", value = "-02:00")},
            emptyText = "Select timezone",
            variant = "default")
    String timezone;

    @DialogField(label="ACS List Options")
    @Select(datasource = @DataSource(
            resourceType = "acs/list/resource/type",
            path = "/path/to/acs/list"
    ))
    String acsListOptions;
}
