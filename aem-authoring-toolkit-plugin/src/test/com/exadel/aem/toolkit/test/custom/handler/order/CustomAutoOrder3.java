package com.exadel.aem.toolkit.test.custom.handler.order;

import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.test.custom.annotation.CustomWidgetAutoOrder;

@Handles(CustomWidgetAutoOrder.class)
public class CustomAutoOrder3 implements DialogWidgetHandler {

    @Override
    public void accept(Source source, Target target) {
        target.attribute("ca3", "ca3");
    }
}
