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

/**
 * The {@link Handler} for a tabbed TouchUI dialog
 */
public class TabsHandler implements Handler, BiConsumer<Class<?>, Element> {
    private static final String DEFAULT_TAB_NAME = "tab1";

    /**
     * Implements {@code BiConsumer<Class<?>, Element>} pattern
     * to process component-backing Java class and append the results to the XML root node
     * @param componentClass {@code Class<?>} instance used as the source of markup
     * @param parentElement XML document root element
     */
    @Override
    public void accept(Class<?> componentClass, Element parentElement) {
        Element tabItems = (Element) parentElement.appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_CONTENT, ResourceTypes.CONTAINER))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_TABS, ResourceTypes.TABS))
                .appendChild(getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS));
        Dialog dialog = componentClass.getDeclaredAnnotation(Dialog.class);
        Tab[] dialogTabs = dialog.tabs();
        if(dialogTabs.length == 0){
            Class[] innerClasses = componentClass.getDeclaredClasses();
            ArrayUtils.reverse(innerClasses);
            Arrays.stream(innerClasses).filter(c -> c.isAnnotationPresent(Tab.class))
                    .forEach(tabClass->addTab(tabClass, tabItems));
            return;
        }
        List<Field> allFields = PluginReflectionUtility.getAllNonStaticFields(componentClass);
        Element firstTabElement = null;
        for (int i = 0; i < dialogTabs.length; i++) {
            Tab tab = dialogTabs[i];
            String nodeName = getXmlUtil().getUniqueName(tab.title(), DEFAULT_TAB_NAME, tabItems);
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
            Handler.appendToContainer(thisTabFields, tabElement);
            allFields.removeAll(thisTabFields);
        }
        if (!allFields.isEmpty()) {
            Handler.appendToContainer(allFields, firstTabElement);
        }
    }

    /**
     * Adds a tab definition to the XML markup
     * @param tabClass The {@code Class<?>} instance to build tab markup from
     * @param itemsNode The {@link Element} instance to append particular fields' markup
     */
    private void addTab(Class<?> tabClass, Element itemsNode){
        Tab tab = tabClass.getAnnotation(Tab.class);
        String nodeName = getXmlUtil().getUniqueName(tab.title(), DEFAULT_TAB_NAME, itemsNode);
        Element tabElement = getXmlUtil().createNodeElement(nodeName, Collections.singletonMap(JcrConstants.PN_TITLE, tab.title()));
        itemsNode.appendChild(tabElement);
        appendAttributes(tabElement, tab);
        Handler.appendToContainer(Arrays.asList(tabClass.getDeclaredFields()), tabElement);
    }

    /**
     * Appends tab attributes to a pre-built tab-defining XML element
     * @param tabElement {@link Element} instance representing a TouchUI dialog tab
     * @param tab {@link Tab} annotation that contains settings
     */
    private void appendAttributes(Element tabElement, Tab tab){
        Attribute attribute = tab.attribute();
        getXmlUtil().mapProperties(tabElement, attribute);
        getXmlUtil().appendDataAttributes(tabElement, attribute.data());
    }

    /**
     * The predicate to match a {@code Field} against particular {@code Tab}
     * @param field  {@link Field} instance to analyze
     * @param tab {@link Tab} annotation to analyze
     * @return True or false
     */
    private static boolean isFieldForTab(Field field, Tab tab) {
        if (!field.isAnnotationPresent(PlaceOnTab.class)) {
            return false;
        }
        return tab.title().equalsIgnoreCase(field.getAnnotation(PlaceOnTab.class).value());
    }
}
