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

package com.exadel.aem.toolkit.samples.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.HtmlTag;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.samples.constants.GroupConstants;
import com.exadel.aem.toolkit.samples.constants.PathConstants;
import com.exadel.aem.toolkit.samples.models.fieldsets.ArmorColorFields;

@AemComponent(
    path = "content/armor-color-component",
    title = "Armor Color Component",
    description = "Choose colors of warrior's armor",
    resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
    componentGroup = GroupConstants.COMPONENT_GROUP
)
@Dialog
@HtmlTag(className = "component-wrap")
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ArmorColorComponent {

    private static final String TITLE_ARMOR_COLOR = "Color of warrior's armor";

    public static final String FIELDS_PREFIX = "color";
    public static final String FIELDS_POSTFIX = "-test";

    @DialogField
    @FieldSet(
        title = TITLE_ARMOR_COLOR,
        namePrefix = ArmorColorComponent.FIELDS_PREFIX,
        namePostfix = ArmorColorComponent.FIELDS_POSTFIX
    )
    @Self
    private ArmorColorFields armorColor;

    public String getHelmetColor() {
        return armorColor.getHelmet();
    }

    public String getArmorColor() {
        return armorColor.getArmor();
    }

    public String getShoesColor() {
        return armorColor.getShoes();
    }
}
