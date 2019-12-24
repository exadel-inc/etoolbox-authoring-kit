package com.exadel.aem.toolkit.samples.models;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Properties;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Dialog(
        name = "content/ext&prop-test-component",
        title = "@Extends and @Property test component",
        description = "Component for testing extends-in and property annotation",
        resourceSuperType = "authoring-toolkit/samples/components/content/father-select-component",
        componentGroup = "Toolkit Samples",
        tabs= {
        @Tab(title = FatherSelectComponent.TAB_MAIN),
}
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ExtendsAndPropertyTestComponent extends FatherSelectComponent{

        @Extends(value = WarriorDescriptionComponent.class, field = "description")
        @RichTextEditor(
                features = {
                        RteFeatures.SEPARATOR,
                        RteFeatures.SPELLCHECK_CHECKTEXT
                })
        @DialogField(name = "./sampleText", label = "Extended RTE with additional feature")
        private String sampleText;

        @Select(options = {
                @Option(text = "C", value = "c"),
                @Option(text = "D", value = "d")
        })
        @DialogField(label = "Select with overrode children")
        @Properties(value = {@Property(name = "sling:hideChildren", value = "*")})
        @ValueMapValue
        private String fatherSelect;

        public String getSampleText() {
                if (sampleText == null || "".equals(sampleText)) {
                        return "@Extends-in and @Property test component";
                }
                return sampleText;
        }
}
