package com.exadel.aem.toolkit;

import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.imageupload.ImageUpload;

@SuppressWarnings("unused")
class SamplePrimaryDialog extends SampleDialog {
    @DialogField(
            name = "First_Field_Primary_Field",
            label = "Label to first primary field",
            description = "Description to first primary field",
            required = true,
            ranking = 1
    )
    @ImageUpload
    private String firstFieldPrimaryField;

    @DialogField(
            name = "Second_Field_Primary_Field",
            label = "Label to second primary field",
            description = "Description to second primary field",
            required = true,
            ranking = 2
    )
    @TextField
    private String secondFieldPrimaryField;

}
