package com.exadel.aem.toolkit.samples.models;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnActions;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.samples.constants.PathConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Dialog(
        name = "content/attribute-test-component",
        title = "Attribute Test Component",
        description = "Component for testing attribute annotation",
        resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
        componentGroup = "Toolkit Samples",
        tabs= {
                @Tab(title = AttributeTestComponent.TAB_MAIN),
        }
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class AttributeTestComponent {

    public static final String TAB_MAIN = "Main";

    @FieldSet(title = "First fieldSet")
    @DialogField(label = "Toggle fieldSet")
    @Self
    private AttributeFieldSet firstFieldSet;

    @Attribute(clas = "toggle-fieldSet")
    @FieldSet(title = "Second fieldSet")
    @DialogField(label = "Passive fieldSet")
    @Self
    private AttributeFieldSet secondFieldSet;

    @DependsOn(query = "@switch (coral-panel |> .toggle-fieldSet)", action = DependsOnActions.DISABLED)
    @TextField
    @DialogField(label = "Toggled text")
    @ValueMapValue
    private String toggledText;

    public String getToggledText() {
        return toggledText;
    }
}
