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
package com.exadel.aem.toolkit.core.handlers.widget.common;

import java.lang.reflect.Member;
import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.util.NamingUtil;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.core.util.DialogConstants;

/**
 * Handler for storing {@link DialogField} properties to a Granite UI widget node
 */
public class DialogFieldHandler implements BiConsumer<Source, Target> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     */
    @Override
    public void accept(Source source, Target target) {
        DialogField dialogField = source.adaptTo(DialogField.class);
        if (dialogField == null) {
            return;
        }
        String name = source.getName();
        if (StringUtils.isNotBlank(dialogField.name())) {
            name = !DialogConstants.PATH_SEPARATOR.equals(dialogField.name()) && !DialogConstants.RELATIVE_PATH_PREFIX.equals(dialogField.name())
                ? NamingUtil.getValidFieldName(dialogField.name())
                : DialogConstants.RELATIVE_PATH_PREFIX;
        }
        String prefix = (String) source.fromValueMap(DialogConstants.PN_PREFIX);
        if (StringUtils.isNotBlank(prefix)
                && !(prefix.equals(DialogConstants.RELATIVE_PATH_PREFIX) && name.equals(DialogConstants.RELATIVE_PATH_PREFIX))
                && !(prefix.equals(DialogConstants.RELATIVE_PATH_PREFIX) && name.startsWith(DialogConstants.PARENT_PATH_PREFIX))) {
            name = prefix + name;
        }
        name = name + source.fromValueMap(DialogConstants.PN_POSTFIX);
        target.attribute(DialogConstants.PN_NAME, name);
    }
}
