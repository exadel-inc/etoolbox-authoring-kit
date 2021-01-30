package com.exadel.aem.toolkit.test.custom.handler.order;

import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.test.custom.annotation.CustomWidgetAutoOrder;

@Handles(value = CustomWidgetAutoOrder.class)
@SuppressWarnings("unused")
public class CustomAutoOrder1 implements DialogWidgetHandler {

    @Override
    public void accept(Source source, Target target) {
        if (source.adaptTo(CustomWidgetAutoOrder.class) != null) {
            target.attribute("ca1", "ca1")
                .attribute("ca2", "ca1")
                .attribute("ca3", "ca1");
        }
    }
}
