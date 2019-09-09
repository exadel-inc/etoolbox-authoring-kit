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
package com.exadel.aem.toolkit.core.handlers.widget;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

public class CheckboxHandler implements Handler, BiConsumer<Element, Field> {
    private static final String POSTFIX_FOR_ROOT_CHECKBOX = "Checkbox";

    @Override
    public void accept(Element element, Field field) {
        Checkbox checkbox = field.getDeclaredAnnotation(Checkbox.class);

        if (checkbox.sublist()[0] == Object.class) {
            getXmlUtil().mapProperties(element, checkbox);
            validateTextProperty(element, field);
        } else {
            element.setAttribute(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, ResourceTypes.NESTED_CHECKBOX_LIST);
            Element itemsElement = getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS);
            element.appendChild(itemsElement);

            Element checkboxElement = getXmlUtil().createNodeElement(field.getName() + POSTFIX_FOR_ROOT_CHECKBOX, ResourceTypes.CHECKBOX);
            getXmlUtil().mapProperties(checkboxElement, checkbox);
            itemsElement.appendChild(checkboxElement);

            appendNestedCheckBoxList(checkboxElement, field);
        }
    }

    private void appendNestedCheckBoxList(Element element, Field field) {
        Element sublist = getXmlUtil().createNodeElement(DialogConstants.NN_SUBLIST, ResourceTypes.NESTED_CHECKBOX_LIST);
        getXmlUtil().setAttribute(sublist, DialogConstants.PN_DISCONNECTED, field.getAnnotation(Checkbox.class).disconnectedSublist());
        element.appendChild(sublist);

        Element itemsElement = getXmlUtil().createNodeElement(DialogConstants.NN_ITEMS);
        sublist.appendChild(itemsElement);

        appendCheckbox(itemsElement, field);
    }

    private void appendCheckbox(Element element, Field field) {
        Checkbox checkbox = field.getAnnotation(Checkbox.class);

        for (Class<?> sublistClass : checkbox.sublist()) {
            List<Field> fields = PluginReflectionUtility.getAllFields(sublistClass);
            fields = fields.stream().filter(f -> f.getAnnotation(Checkbox.class) != null).collect(Collectors.toList());

            for (Field innerField : fields) {
                Element checkboxElement = getXmlUtil().createNodeElement(innerField.getName(), ResourceTypes.CHECKBOX);
                getXmlUtil().mapProperties(checkboxElement, innerField.getAnnotation(Checkbox.class));
                validateTextProperty(checkboxElement, innerField);

                element.appendChild(checkboxElement);

                if (innerField.getAnnotation(Checkbox.class).sublist()[0] != Object.class) {
                    appendNestedCheckBoxList(checkboxElement, innerField);
                }
            }

        }
    }

    private void validateTextProperty(Element checkboxElement, Field field) {
        Checkbox checkbox = field.getAnnotation(Checkbox.class);
        if (checkbox.text().isEmpty() && field.getAnnotation(DialogField.class) != null) {
            checkboxElement.setAttribute(DialogConstants.PN_TEXT, field.getAnnotation(DialogField.class).label());
        } else if (checkbox.text().isEmpty()) {
            checkboxElement.setAttribute(DialogConstants.PN_TEXT, field.getName());
        }
    }
}
