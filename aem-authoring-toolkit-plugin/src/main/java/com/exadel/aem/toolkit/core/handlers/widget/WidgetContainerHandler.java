package com.exadel.aem.toolkit.core.handlers.widget;

import com.exadel.aem.toolkit.api.annotations.widgets.AccordionWidget;
import com.exadel.aem.toolkit.api.annotations.widgets.TabsWidget;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.handlers.container.common.ContainerHandler;
import com.exadel.aem.toolkit.core.handlers.container.common.TabContainerInstance;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

abstract class WidgetContainerHandler implements WidgetSetHandler {

    void acceptParent(Element element, Class<? extends Annotation> annotation, Field field) {
        String defaultTabName = annotation.equals(TabsWidget.class) ? "tab" : "accordion";
        String exceptionMessage = annotation.equals(TabsWidget.class) ? "No tabs defined for the dialog at " : "No accordions defined for the dialog at ";
        Element tabItemsElement = (Element) element
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS));

        Map<String, TabContainerInstance> tabInstancesFromCurrentClass = getInstancesFromCurrentClass(annotation, field);

        Class<?> tabsType = field.getType();
        List<Field> allFields = getContainerFields(element, field, tabsType);

        if (tabInstancesFromCurrentClass.isEmpty() && !allFields.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                    exceptionMessage + tabsType.getName()
            ));
            if (annotation.equals(TabsWidget.class)) {
                tabInstancesFromCurrentClass.put(StringUtils.EMPTY, new TabContainerInstance("newTab"));
            }
        }

        Iterator<Map.Entry<String, TabContainerInstance>> tabInstanceIterator = tabInstancesFromCurrentClass.entrySet().iterator();
        int iterationStep = 0;
        String[] array = {};

        ContainerHandler.addTab(tabInstanceIterator, iterationStep, allFields, array, tabItemsElement, defaultTabName);
        ContainerHandler.handleInvalidTabException(allFields);
    }

    private Map<String, TabContainerInstance> getInstancesFromCurrentClass(Class<? extends Annotation> annotation, Field field) {
        Map<String, TabContainerInstance> tabInstancesFromCurrentClass = new LinkedHashMap<>();
        if (field.isAnnotationPresent(annotation)) {
            if (annotation.equals(TabsWidget.class)) {
                Arrays.stream(field.getAnnotation(TabsWidget.class).tabs())
                        .forEach(tab -> {
                            TabContainerInstance tabContainerInstance = new TabContainerInstance(tab.title());
                            tabContainerInstance.setAttributes(ContainerHandler.getAnnotationFields(tab));
                            tabInstancesFromCurrentClass.put(tab.title(), tabContainerInstance);
                        });
            } else if (annotation.equals(AccordionWidget.class)) {
                Arrays.stream(field.getAnnotation(AccordionWidget.class).panels())
                        .forEach(tab -> {
                            TabContainerInstance tabContainerInstance = new TabContainerInstance(tab.title());
                            tabContainerInstance.setAttributes(ContainerHandler.getAnnotationFields(tab));
                            tabInstancesFromCurrentClass.put(tab.title(), tabContainerInstance);
                        });
            }
        }
        return tabInstancesFromCurrentClass;
    }
}
