package com.exadel.aem.toolkit.core.handlers.widget;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.widgets.AccordionWidget;
import com.exadel.aem.toolkit.api.annotations.widgets.TabsWidget;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.handlers.container.common.ContainerHandler;
import com.exadel.aem.toolkit.core.handlers.container.common.ContainerInfo;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;

abstract class WidgetContainerHandler implements Handler, BiConsumer<Element, Field>, WidgetHandlerUtils {

    void acceptParent(Element element, Class<? extends Annotation> annotation, Field field) {
        String defaultTabName = annotation.equals(TabsWidget.class) ? ContainerHandler.TAB : ContainerHandler.ACCORDION;
        String exceptionMessage = annotation.equals(TabsWidget.class) ? ContainerHandler.TABS_EXCEPTION : ContainerHandler.ACCORDION_EXCEPTON;
        Element tabItemsElement = (Element) element
            .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS));

        Map<String, ContainerInfo> tabInstancesFromCurrentClass = getInstancesFromCurrentClass(annotation, field);

        Class<?> tabsType = field.getType();
        List<Field> allFields = WidgetHandlerUtils.getContainerFields(element, field, tabsType);

        if (tabInstancesFromCurrentClass.isEmpty() && !allFields.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                exceptionMessage + tabsType.getName()
            ));
            if (annotation.equals(TabsWidget.class)) {
                tabInstancesFromCurrentClass.put(StringUtils.EMPTY, new ContainerInfo("newTab"));
            }
        }

        ContainerHandler.addTabs(tabInstancesFromCurrentClass, allFields, ArrayUtils.EMPTY_STRING_ARRAY, tabItemsElement, defaultTabName);
        ContainerHandler.handleInvalidTabException(allFields);
    }

    /**
     * Get all tabs from current class.
     * @param annotation {@link Class<? extends Annotation>} searching annotation
     * @param field      Current {@code Field} instance
     */
    private Map<String, ContainerInfo> getInstancesFromCurrentClass(Class<? extends Annotation> annotation, Field field) {
        Map<String, ContainerInfo> tabInstancesFromCurrentClass = new LinkedHashMap<>();
        if (field.isAnnotationPresent(annotation)) {
            if (annotation.equals(TabsWidget.class)) {
                Arrays.stream(field.getAnnotation(TabsWidget.class).tabs())
                    .forEach(tab -> {
                        ContainerInfo containerInfo = new ContainerInfo(tab.title());
                        containerInfo.setAttributes(ContainerHandler.getAnnotationFields(tab));
                        tabInstancesFromCurrentClass.put(tab.title(), containerInfo);
                    });
            } else if (annotation.equals(AccordionWidget.class)) {
                Arrays.stream(field.getAnnotation(AccordionWidget.class).panels())
                    .forEach(tab -> {
                        ContainerInfo containerInfo = new ContainerInfo(tab.title());
                        containerInfo.setAttributes(ContainerHandler.getAnnotationFields(tab));
                        tabInstancesFromCurrentClass.put(tab.title(), containerInfo);
                    });
            }
        }
        return tabInstancesFromCurrentClass;
    }
}
