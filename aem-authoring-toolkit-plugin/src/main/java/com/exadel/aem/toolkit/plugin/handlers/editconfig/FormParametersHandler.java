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
package com.exadel.aem.toolkit.plugin.handlers.editconfig;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.FormParameter;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;

/**
 * {@code BiConsumer<EditConfig, Target>} implementation for storing {@link FormParameter} arguments to {@code cq:editConfig} XML node
 */
public class FormParametersHandler implements BiConsumer<EditConfig, Target> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param editConfig {@code EditConfig} annotation instance
     * @param root Current {@link Target} instance
     */
    @Override
    public void accept(EditConfig editConfig, Target root) {
        if(editConfig.formParameters().length == 0){
            return;
        }
        Map<String, Object> propertiesMap = Arrays.stream(editConfig.formParameters())
            .collect(Collectors.toMap(FormParameter::name, FormParameter::value));
        root.getOrCreateTarget(DialogConstants.NN_FORM_PARAMETERS).attributes(propertiesMap);
    }
}
