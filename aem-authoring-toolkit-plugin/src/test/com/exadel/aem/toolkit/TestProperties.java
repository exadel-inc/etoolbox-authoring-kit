package com.exadel.aem.toolkit;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

@Dialog(
        name = "myComponent",
        title = "My AEM Component",
        description = "The most awesome AEM component ever",
        componentGroup = "my-brand-new-components",
        templatePath = "/some/absolute/jcr/path",
        resourceSuperType = "/path/to/resource",
        cellName = "some-cell-name",
        helpPath = "https://www.google.com/search?q=my+aem+component",
        isContainer = true,
        width = 800,
        height = 600,
        layout = DialogLayout.FIXED_COLUMNS
)
@SuppressWarnings("unused")
public class TestProperties {
    @DialogField
    @TextField
    String field;
}
