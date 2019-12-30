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
        private final String DEFAULT_SELECT_TEXT = "nothing is selected";

        @Select(options = {
                @Option(text = "A", value = "a"),
                @Option(text = "B", value = "b")
        })
        @DialogField(label = "Dungeons select")
        @ValueMapValue
        private String dungeonsSelect;

        public String getFatherSelect() { return (dungeonsSelect == null) ? DEFAULT_SELECT_TEXT : dungeonsSelect; }
}
