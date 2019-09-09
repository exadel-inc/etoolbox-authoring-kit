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

import java.util.function.BiConsumer;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfigLayout;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;

public class PropertiesHandler implements Handler, BiConsumer<Element, EditConfig> {
    @Override
    public void accept(Element root, EditConfig editConfig) {
        getXmlUtil().mapProperties(root, editConfig);
        EditConfigLayout dialogLayout = editConfig.dialogLayout();
        if (dialogLayout != EditConfigLayout.DEFAULT) {
            root.setAttribute(DialogConstants.PN_DIALOG_LAYOUT, dialogLayout.toString().toLowerCase());
        }
    }
}
