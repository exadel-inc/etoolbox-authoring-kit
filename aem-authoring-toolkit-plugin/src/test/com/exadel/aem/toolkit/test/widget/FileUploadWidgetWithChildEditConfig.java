package com.exadel.aem.toolkit.test.widget;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.Action;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;

import static com.exadel.aem.toolkit.core.util.TestConstants.DEFAULT_COMPONENT_NAME;

@Dialog(
        name = DEFAULT_COMPONENT_NAME,
        title = "FileUpload Widget Dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@ChildEditConfig(
        actions = {
                Action.DELETE,
                Action.COPY_MOVE,
                Action.EDIT,
                Action.INSERT
        }
)
@SuppressWarnings("unused")
public class FileUploadWidgetWithChildEditConfig extends FileUploadWidget {
}
