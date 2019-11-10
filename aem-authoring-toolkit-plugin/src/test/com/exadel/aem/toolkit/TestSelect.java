package com.exadel.aem.toolkit;

import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.DialogLayout;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.api.annotations.widgets.select.StatusVariantConstants;

@Dialog(
        name = "test-component",
        title = "test-component-dialog",
        layout = DialogLayout.FIXED_COLUMNS
)
@SuppressWarnings("unused")
public class TestSelect {
    @DialogField(label = "Rating")
    @Select(options = {
            @Option(
                    text = "1 star",
                    value = "1",
                    selected = true,
                    statusIcon = "/content/dam/samples/icons/1-star-rating.png",
                    statusText = "This is to set 1-star rating",
                    statusVariant = StatusVariantConstants.SUCCESS
            ),
            @Option(text = "2 stars", value = "2"),
            @Option(text = "3 stars", value = "3"),
            @Option(text = "4 stars", value = "4", disabled=true),
            @Option(text = "5 stars", value = "5", disabled=true)
    }, emptyText = "Select rating")
    String rating;

    @DialogField(label = "Timezone")
    @Select(options = {
            @Option(text = "UTC +2", value = "+02:00"),
            @Option(text = "UTC +1", value = "+01:00"),
            @Option(text = "UTC", value = "00:00"),
            @Option(text = "UTC -1", value = "-01:00"),
            @Option(text = "UTC -2", value = "-02:00")
    }, emptyText = "Select timezone")
    String timezone;

}
