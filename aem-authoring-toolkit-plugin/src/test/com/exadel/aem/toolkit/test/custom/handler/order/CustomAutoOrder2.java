package com.exadel.aem.toolkit.test.custom.handler.order;

import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.test.custom.annotation.CustomWidgetAutoOrder;

@Handles(value = CustomWidgetAutoOrder.class)
@SuppressWarnings("unused")
public class CustomAutoOrder2 implements DialogWidgetHandler {

    @Override
    public void accept(Source source, Target target) {
        target.attribute("ca2", "ca2")
            .attribute("ca3", "ca2");
    }
}
