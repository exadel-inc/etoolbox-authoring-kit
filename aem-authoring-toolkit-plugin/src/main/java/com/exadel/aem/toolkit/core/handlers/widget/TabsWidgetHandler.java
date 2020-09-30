/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.aem.toolkit.core.handlers.widget;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.JcrConstants;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.TabsWidget;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.handlers.container.common.CommonTabUtils;
import com.exadel.aem.toolkit.core.handlers.container.common.TabInstance;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginObjectUtility;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;

import java.lang.reflect.Field;
import java.util.*;

/**
 * The {@link Handler} for a tabbed TouchUI dialog
 */
public class TabsWidgetHandler implements WidgetSetHandler {
    private static final String DEFAULT_TAB_NAME = "tab";
    private static final String NO_TABS_DEFINED_EXCEPTION_MESSAGE = "No tabs defined for the dialog at ";

    /**
     * Processes the user-defined data and writes it to XML entity
     *
     * @param element Current XML element
     * @param field   Current {@code Field} instance
     */
    @Override
    public void accept(Element element, Field field) {

        Element tabItemsElement = (Element) element
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS));

        Map<String, TabInstance> tabInstancesFromCurrentClass = new LinkedHashMap<>();
        if (field.isAnnotationPresent(TabsWidget.class)) {
            Arrays.stream(field.getAnnotation(TabsWidget.class).tabs())
                    .forEach(tab -> tabInstancesFromCurrentClass.put(tab.title(), new TabInstance(tab)));
        }
        Class<?> tabsType = field.getType();

        List<Field> allFields = getContainerFields(element, field, tabsType);


        if (tabInstancesFromCurrentClass.isEmpty() && !allFields.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                    NO_TABS_DEFINED_EXCEPTION_MESSAGE + tabsType.getName()
            ));
            Tab newTab = PluginObjectUtility.create(Tab.class,
                    Collections.singletonMap(DialogConstants.PN_TITLE, StringUtils.EMPTY));
            tabInstancesFromCurrentClass.put(StringUtils.EMPTY, new TabInstance(newTab));
        }

        Iterator<Map.Entry<String, TabInstance>> tabInstanceIterator = tabInstancesFromCurrentClass.entrySet().iterator();
        int iterationStep = 0;

        while (tabInstanceIterator.hasNext()) {
            final boolean isFirstTab = iterationStep++ == 0;
            TabInstance currentTabInstance = tabInstanceIterator.next().getValue();
            List<Field> storedCurrentTabFields = CommonTabUtils.getStoredCurrentTabFields(allFields, currentTabInstance, isFirstTab);
            appendTab(tabItemsElement, currentTabInstance.getTab(), storedCurrentTabFields);
        }
        CommonTabUtils.handleInvalidTabException(allFields);
    }

    private void appendTab(Element tabCollectionElement, Tab tab, List<Field> fields) {
        String nodeName = getXmlUtil().getUniqueName(tab.title(), DEFAULT_TAB_NAME, tabCollectionElement);
        Element tabElement = getXmlUtil().createNodeElement(
                nodeName,
                ImmutableMap.of(
                        JcrConstants.PN_TITLE, tab.title(),
                        JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.CONTAINER
                ));
        tabCollectionElement.appendChild(tabElement);
        appendTabAttributes(tabElement, tab);
        Handler.appendToContainer(tabElement, fields);
    }

    private void appendTabAttributes(Element tabElement, Tab tab) {
        tabElement.setAttribute(JcrConstants.PN_TITLE, tab.title());
        Attribute attribute = tab.attribute();
        getXmlUtil().mapProperties(tabElement, attribute);
        getXmlUtil().appendDataAttributes(tabElement, attribute.data());
    }
}
