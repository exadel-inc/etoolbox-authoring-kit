package com.exadel.aem.toolkit.samples.models;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnActions;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnParam;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.*;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.samples.constants.PathConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Dialog(
        name = "content/abilities-component",
        title = "Abilities",
        description = "Abilities of your warrior",
        resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
        componentGroup = "Toolkit Samples",
        tabs = {
                @Tab(title = AbilitiesComponent.TAB_ABILITIES)
        }
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class AbilitiesComponent {

    static final String TAB_ABILITIES = "Abilities";

    @DependsOnRef(name = "ability")
    @PlaceOnTab(AbilitiesComponent.TAB_ABILITIES)
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
    @DialogField(
            name = "./ability",
            ranking = 2
    )
    @ValueMapValue
    private String ability;

    @DependsOn(
            query = "@this.length <= 3",
            action = DependsOnActions.VALIDATE,
            params = { @DependsOnParam(name = "msg", value = "Too powerful!")}
            )
    @DependsOn(query = "@ability === 'magic'")
    @PlaceOnTab(AbilitiesComponent.TAB_ABILITIES)
    @MultiField(field = Element.class)
    @DialogField(
            label = "Elements",
            name = "./elements",
            description = "Add elements that your magician owns",
            ranking = 3
    )
    @ValueMapValue(name = "./element")
    private String[] elements;

    public class Element {
        @TextField
        @DialogField
        public String element;
    }

    @PlaceOnTab(AbilitiesComponent.TAB_ABILITIES)
    @NumberField(min = 0, max = 100)
    @DialogField(
            name = "./abilityLevel",
            label = "Warrior experience",
            description = "Enter your warrior ability level",
            ranking = 1
    )
    @ValueMapValue
    private int abilityLevel;

    public String getAbility() { return ability; }

    public String[] getElements() {
        return (elements != null)? elements : new String[0];
    }

    public String getAbilityLevel() { return String.valueOf(abilityLevel); }
}
