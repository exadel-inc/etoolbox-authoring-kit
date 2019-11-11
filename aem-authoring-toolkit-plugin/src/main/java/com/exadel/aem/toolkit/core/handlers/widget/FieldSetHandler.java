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

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.PluginReflectionUtility;

public class FieldSetHandler implements Handler, BiConsumer<Element, Field> {
    @Override
    public void accept(Element element, Field field) {
        Class<?> fieldSetClass = field.getType();
        FieldSet fieldSet = field.getDeclaredAnnotation(FieldSet.class);
        String restoredNamePrefix = getXmlUtil().getNamePrefix();
        if (StringUtils.isNotBlank(fieldSet.namePrefix())) {
            getXmlUtil().setNamePrefix(restoredNamePrefix + getXmlUtil().getValidName(fieldSet.namePrefix()));
        }

        List<Field> fields = PluginReflectionUtility.getAllNonStaticFields(fieldSetClass);

        List<Field> ignoreFields = PluginReflectionUtility.getAllIgnoredFields(fieldSetClass);
        if(!ignoreFields.isEmpty()) {
            fields = fields.stream()
                    .filter(f -> ignoreFields.stream()
                            .anyMatch(ignoreField -> !f.getName().equals(ignoreField.getName())))
                    .collect(Collectors.toList());
        }

        Handler.appendToContainer(fields, element, false);
        getXmlUtil().setNamePrefix(restoredNamePrefix);
    }
}