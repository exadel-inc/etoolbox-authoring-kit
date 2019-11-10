package com.exadel.aem.toolkit;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@SuppressWarnings("unused")
public class TestNestedCheckboxList {
    @Checkbox(text = "Level 1 Checkbox", sublist = Sublist.class)
    boolean option1L1;

    static class Sublist {
        @Checkbox(text = "Level 2 Checkbox 1")
        boolean option2L1;

        @Checkbox(text = "Level 2 Checkbox 2", sublist = Sublist2.class)
        boolean option2L2;
    }

    private static class Sublist2 {
        @Checkbox(text = "Level 3 Checkbox 1")
        boolean option3L1;

        @Checkbox(text = "Level 3 Checkbox 2")
        boolean option3L2;
    }
}
