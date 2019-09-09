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
package com.exadel.aem.toolkit.core.handlers.editconfig;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.FormParameter;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;

public class FormParametersHandler implements Handler, BiConsumer<Element, EditConfig> {
    @Override
    public void accept(Element root, EditConfig editConfig) {
        if(editConfig.formParameters().length == 0){
            return;
        }
        Map<String, String> propertiesMap = Arrays.stream(editConfig.formParameters())
            .collect(Collectors.toMap(FormParameter::name, FormParameter::value));
        Element formParametersElement = getXmlUtil().createNodeElement(DialogConstants.NN_FORM_PARAMETERS, propertiesMap);
        root.appendChild(formParametersElement);
    }
}
