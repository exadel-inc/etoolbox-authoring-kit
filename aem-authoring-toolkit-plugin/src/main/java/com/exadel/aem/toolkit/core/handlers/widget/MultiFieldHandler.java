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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;

/**
 * {@link Handler} implementation used to create markup responsible for Granite UI {@code Multifield} widget functionality
 * within the {@code cq:dialog} XML node
 */
public class MultiFieldHandler implements Handler, BiConsumer<Element, Field> {
    private static final String EMPTY_CLASS_EXCEPTION_MESSAGE = "Empty multifield class ";

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param element Current XML element
     * @param field Current {@code Field} instance
     */
    @Override
    public void accept(Element element, Field field) {
        MultiField multiField = field.getDeclaredAnnotation(MultiField.class);
        String name = element.getAttribute(DialogConstants.PN_NAME);
        element.removeAttribute(DialogConstants.PN_NAME);
        Class<?> fieldClass = multiField.field();
        List<Field> fieldClassFields = Arrays.stream(fieldClass.getDeclaredFields())
            .filter(DialogComponent::isPresent).collect(Collectors.toList());
        if(fieldClassFields.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(
                    EMPTY_CLASS_EXCEPTION_MESSAGE + fieldClass.getName()
            ));
            return;
        }
        if(fieldClassFields.size() > 1){
            getXmlUtil().setAttribute(element, DialogConstants.PN_COMPOSITE, true);
            Element containerElement = PluginRuntime.context().getXmlUtility().createNodeElement(DialogConstants.NN_FIELD);
            containerElement.setAttribute(DialogConstants.PN_NAME, name);
            element.appendChild(containerElement);
            // in case there are multiple fields in multifield container, their "name" values must not be preceded
            // with "./" which is by default
            // see https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/multifield/index.html#examples
            String restoredNamePrefix = getXmlUtil().getNamePrefix();
            getXmlUtil().setNamePrefix(restoredNamePrefix.startsWith(DialogConstants.RELATIVE_PATH_PREFIX) ? restoredNamePrefix.substring(2) : restoredNamePrefix);
            Handler.appendToContainer(fieldClassFields, containerElement);
            getXmlUtil().setNamePrefix(restoredNamePrefix);
            return;
        }
        DialogComponent.fromField(fieldClassFields.get(0))
                .ifPresent(comp -> comp.append(element, fieldClassFields.get(0), DialogConstants.NN_FIELD));
    }
}
