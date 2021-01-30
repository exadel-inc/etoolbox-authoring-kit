package com.exadel.aem.toolkit.test.custom.handler;

import java.lang.reflect.Field;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.test.custom.annotation.CustomWidgetAnnotation;

@Handles(value = CustomWidgetAnnotation.class, after = CustomWidgetHandler2.class)
@SuppressWarnings("unused")
public class CustomWidgetHandler3 implements DialogWidgetHandler {

    @Override
    public void accept(Element element, Field field) {
        element.setAttribute("cwh3", "cwh3");
    }
}
