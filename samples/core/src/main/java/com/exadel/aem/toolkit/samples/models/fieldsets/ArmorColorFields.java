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

package com.exadel.aem.toolkit.samples.models.fieldsets;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.color.ColorField;
import com.exadel.aem.toolkit.samples.models.ArmorColorComponent;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ArmorColorFields {

    private static final String DEFAULT_COLOR = "#A9A9A9";

    @DialogField
    @ColorField(
            emptyText = "Choose armor color"
    )
    @Place(
        after = @ClassMember("helmet"),
        before = @ClassMember("shoes")
    )
    @Default(values = DEFAULT_COLOR)
    @ValueMapValue(name = ArmorColorComponent.FIELDS_PREFIX + "armor" + ArmorColorComponent.FIELDS_POSTFIX)
    private String armor;

    @DialogField
    @ColorField(
            emptyText = "Choose shoes color"
    )
    @Default(values = DEFAULT_COLOR)
    @ValueMapValue(name = ArmorColorComponent.FIELDS_PREFIX + "shoes" + ArmorColorComponent.FIELDS_POSTFIX)
    private String shoes;

    @DialogField
    @ColorField(
            emptyText = "Choose helmet color"
    )
    @Default(values = DEFAULT_COLOR)
    @ValueMapValue(name = ArmorColorComponent.FIELDS_PREFIX + "helmet" + ArmorColorComponent.FIELDS_POSTFIX)
    private String helmet;

    public String getArmor() {
        return armor;
    }

    public String getShoes() {
        return shoes;
    }

    public String getHelmet() {
        return helmet;
    }
}

