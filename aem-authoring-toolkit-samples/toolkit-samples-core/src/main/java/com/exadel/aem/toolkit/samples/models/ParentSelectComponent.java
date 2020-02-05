package com.exadel.aem.toolkit.samples.models;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;

@Dialog(
        name = "content/parent-select-component",
        title = "Parent Select Component",
        tabs = {
                @Tab(title = ParentSelectComponent.TAB_MAIN),
        }
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ParentSelectComponent {

    static final String TAB_MAIN = "Main";
    private static final String DEFAULT_SELECT_TEXT = "nothing is selected";

    @Select(options = {
            @Option(text = "A", value = "a"),
            @Option(text = "B", value = "b")
    })
    @DialogField(label = "Dungeons select")
    @ValueMapValue
    private String dungeonsSelect;

    public String getFatherSelect() {
        return StringUtils.defaultIfBlank(dungeonsSelect, DEFAULT_SELECT_TEXT);
    }
}
