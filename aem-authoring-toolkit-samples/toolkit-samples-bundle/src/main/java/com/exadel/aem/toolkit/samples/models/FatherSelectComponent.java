package com.exadel.aem.toolkit.samples.models;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Dialog(
        name = "content/father-select-component",
        title = "Father Select Component",
        tabs= {
                @Tab(title = FatherSelectComponent.TAB_MAIN),
        }
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class FatherSelectComponent {

        public static final String TAB_MAIN = "Main";

        @Select(options = {
                @Option(text = "A", value = "a"),
                @Option(text = "B", value = "b")
        })
        @DialogField(label = "Select with overrode children")
        @ValueMapValue
        private String fatherSelect;

        public String getFatherSelect() {
                if (fatherSelect == null) {
                        return "nothing is selected";
                }
                return fatherSelect;
        }
}
