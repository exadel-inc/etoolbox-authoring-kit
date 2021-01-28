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

import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfigLayout;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;

/**
 * {@code BiConsumer<EditConfig, Target>} implementation for storing {@link EditConfig} properties to {@code cq:editConfig} node
 */
public class PropertiesHandler implements BiConsumer<EditConfig, Target> {
    /**
     * Processes the user-defined data and writes it to {@link Target}
     * @param editConfig {@code EditConfig} annotation instance
     * @param root Current {@link Target} instance
     */
    @Override
    public void accept(EditConfig editConfig, Target root) {
        root.attributes(editConfig);
        EditConfigLayout dialogLayout = editConfig.dialogLayout();
        if (dialogLayout != EditConfigLayout.DEFAULT) {
            root.attribute(DialogConstants.PN_DIALOG_LAYOUT, dialogLayout.toString().toLowerCase());
        }
    }
}
