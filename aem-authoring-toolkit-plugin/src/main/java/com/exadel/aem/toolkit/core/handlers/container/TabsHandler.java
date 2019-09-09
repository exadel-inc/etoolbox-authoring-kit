/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package com.exadel.aem.toolkit.core.handlers.container;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.main.JcrConstants;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

public class TabsHandler implements Handler, BiConsumer<Class<?>, Element> {
    private static final String DEFAULT_TAB_NAME = "tab1";
    private static final String INVALID_TAB_NAME_PATTERN = "[^\\w]";

    @Override
    public void accept(Class<?> clazz, Element parent) {
        Element tabItems = (Element) parent.appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_CONTENT, ResourceTypes.CONTAINER))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_TABS, ResourceTypes.TABS))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS));
        Dialog dialog = clazz.getDeclaredAnnotation(Dialog.class);
        Tab[] dialogTabs = dialog.tabs();
        if(dialogTabs.length == 0){
            Class[] innerClasses = clazz.getDeclaredClasses();
            ArrayUtils.reverse(innerClasses);
            Arrays.stream(innerClasses).filter(c -> c.isAnnotationPresent(Tab.class))
                    .forEach(tabClass->addTab(tabClass, tabItems));
            return;
        }
        List<Field> allFields = PluginReflectionUtility.getAllNonStaticFields(clazz);
        Element firstTabElement = null;
        for (int i = 0; i < dialogTabs.length; i++){
            Tab tab = dialogTabs[i];
            String nodeName = getXmlUtil().getUniqueName(tab.title(), INVALID_TAB_NAME_PATTERN, DEFAULT_TAB_NAME, tabItems);
            Element tabElement = (Element) tabItems.appendChild(getXmlUtil().createNodeElement(nodeName));
            tabElement.setAttribute(JcrConstants.PN_TITLE, tab.title());
            appendAttributes(tabElement, tab);
            // the first tab will contain the elements explicitly assigned to it
            // and also all the elements with no tab or erroneous tab specified
            // so we render the first tab after all the rest
            if (i == 0) {
                firstTabElement = tabElement;
                continue;
            }
            List<Field> thisTabFields = allFields.stream()
                    .filter(f -> isFieldForTab(f, tab))
                    .collect(Collectors.toList());
            Handler.appendContainer(thisTabFields, tabElement);
            allFields.removeAll(thisTabFields);
        }
        if (!allFields.isEmpty()) {
            Handler.appendContainer(allFields, firstTabElement);
        }
    }

    private void addTab(Class<?> tabClass, Element itemsNode){
        Tab tab = tabClass.getAnnotation(Tab.class);
        String nodeName = getXmlUtil().getUniqueName(tab.title(), INVALID_TAB_NAME_PATTERN, DEFAULT_TAB_NAME, itemsNode);
        Element tabElement = getXmlUtil().createNodeElement(nodeName, Collections.singletonMap(JcrConstants.PN_TITLE, tab.title()));
        itemsNode.appendChild(tabElement);
        appendAttributes(tabElement, tab);
        Handler.appendContainer(Arrays.asList(tabClass.getDeclaredFields()), tabElement);
    }

    private void appendAttributes(Element tabElement, Tab tab){
        Attribute attribute = tab.attribute();
        getXmlUtil().mapProperties(tabElement, attribute);
        getXmlUtil().appendDataAttributes(tabElement, attribute.data());
    }

    private static boolean isFieldForTab(Field field, Tab tab) {
        if (!field.isAnnotationPresent(PlaceOnTab.class)) {
            return false;
        }
        return tab.title().equalsIgnoreCase(field.getAnnotation(PlaceOnTab.class).value());
    }
}
