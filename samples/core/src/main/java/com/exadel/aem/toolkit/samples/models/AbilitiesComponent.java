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

import java.util.Collections;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnActions;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnParam;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.ClassMember;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Heading;
import com.exadel.aem.toolkit.api.annotations.widgets.NumberField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Multiple;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.samples.constants.GroupConstants;
import com.exadel.aem.toolkit.samples.constants.PathConstants;

@AemComponent(
    path = "content/abilities-component",
    title = "Abilities Component",
    description = "Abilities of your warrior",
    resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
    componentGroup = GroupConstants.COMPONENT_GROUP
)
@Dialog(
    extraClientlibs = "authoring-toolkit.samples.authoring"
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class AbilitiesComponent {

    private static final String FIELD_ELEMENTS = "./elements";
    private static final String LABEL_ELEMENTS = "Elements";
    private static final String DESCRIPTION_ELEMENTS = "Add elements that your magician owns";

    private static final String LABEL_ABILITY_LEVEL = "Warrior experience";
    private static final String DESCRIPTION_ABILITY_LEVEL = "Enter your warrior ability level";

    private static final String TAB_ABILITIES = "Abilities";

    @Heading(value = "Here you can choose abilities", level = 2)
    private String heading;

    @DialogField
    @Select(
        options = {
            @Option(
                selected = true,
                text = "Intelligence",
                value = "intelligence"
            ),
            @Option(
                text = "Strength",
                value = "strength"
            ),
            @Option(
                text = "Magic",
                value = "magic"
            )
        },
        emptyText = "Select ability")
    @DependsOnRef(name = "ability")
    @Place(AbilitiesComponent.TAB_ABILITIES)
    @ValueMapValue
    private String ability;

    @DialogField(
        name = FIELD_ELEMENTS,
        label = LABEL_ELEMENTS,
        description = DESCRIPTION_ELEMENTS
    )
    @TextField
    @Multiple
    @DependsOn(
        query = "@this.length <= 3",
        action = DependsOnActions.VALIDATE,
        params = @DependsOnParam(name = "msg", value = "Too powerful!")
    )
    @DependsOn(query = "@ability === 'magic'")
    @Place(
        value = AbilitiesComponent.TAB_ABILITIES,
        after = @ClassMember("ability")
    )
    @ValueMapValue(name = FIELD_ELEMENTS)
    private List<String> elements;

    @DialogField(
        label = LABEL_ABILITY_LEVEL,
        description = DESCRIPTION_ABILITY_LEVEL
    )
    @NumberField(min = 0, max = 100)
    @Place(
        value = AbilitiesComponent.TAB_ABILITIES,
        after = @ClassMember("heading"),
        before = @ClassMember("ability")
    )
    @ValueMapValue
    private int abilityLevel;

    public String getAbility() {
        return ability;
    }

    public List<String> getElements() {
        return (elements != null) ? elements : Collections.emptyList();
    }

    public String getAbilityLevel() {
        return String.valueOf(abilityLevel);
    }
}
