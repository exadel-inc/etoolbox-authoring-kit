package com.exadel.aem.toolkit.test.custom.handler;

import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.test.custom.annotation.CustomWidgetAnnotation;

@Handles(value = CustomWidgetAnnotation.class, after = CustomWidgetHandler1.class)
@SuppressWarnings("unused")
public class CustomWidgetHandler2 implements DialogWidgetHandler {

    @Override
    public void accept(Source source, Target target) {
        CustomWidgetAnnotation annotation = source.adaptTo(CustomWidgetAnnotation.class);
        if (target.getAttributes().get("customField").equals(annotation.customField())) {
            target.attribute("cwh2", "cwh2")
                .attribute("cwh3", "cwh2");
        }
    }
}
