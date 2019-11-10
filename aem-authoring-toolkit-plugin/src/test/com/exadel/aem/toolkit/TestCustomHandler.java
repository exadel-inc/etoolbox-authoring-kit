package com.exadel.aem.toolkit;

import java.lang.reflect.Field;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.runtime.Injected;
import com.exadel.aem.toolkit.api.runtime.RuntimeContext;

public class TestCustomHandler implements DialogWidgetHandler {
    @Injected
    private RuntimeContext runtimeContext;

    @Override
    public String getName() {
        return "testCustomAnnotation";
    }

    @Override
    public void accept(Element element, Field field) {
        CustomAnnotation testCustomAnnotation = field.getDeclaredAnnotation(CustomAnnotation.class);
        element.setAttribute("customField", testCustomAnnotation.customField());
    }
}
