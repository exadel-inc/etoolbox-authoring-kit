package com.exadel.aem.toolkit;

import java.lang.reflect.Field;

import javax.inject.Inject;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.widgets.DialogWidgets;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.handlers.DialogWidgetHandler;
import com.exadel.aem.toolkit.api.runtime.RuntimeContext;

@DialogWidgets(TextField.class)
public class TestCustomHandler implements DialogWidgetHandler {
    @Inject
    private RuntimeContext runtimeContext;

    @Override
    public String getName() {
        return "testCustomAnnotation";
    }

    public RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    @Override
    public void accept(Element element, Field field) {
        TestCustomAnnotation testCustomAnnotation = field.getAnnotationsByType(TestCustomAnnotation.class)[0];
        element.setAttribute("customField", testCustomAnnotation.customField());
    }
}
