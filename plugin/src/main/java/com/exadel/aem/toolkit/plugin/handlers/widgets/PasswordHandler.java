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
package com.exadel.aem.toolkit.plugin.handlers.widgets;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Password;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.NamingUtil;

/**
 * {@code BiConsumer<Source, Target>} implementation used to create markup responsible for {@code Password} widget functionality
 * within the {@code cq:dialog} node
 */
@Handles(Password.class)
public class PasswordHandler implements Handler {
    /**
     * Processes the user-defined data and writes it to {@link Target}
     * @param source Current {@link Source} instance
     * @param target Current {@link Target} instance
     */
    @Override
    public void accept(Source source, Target target) {
        Password password = source.adaptTo(Password.class);
        if(source.adaptTo(DialogField.class) == null || password.retype().isEmpty()) {
            return;
        }
        // "Retype name" is expected to preserve same structure (prefix. postfix) as the original name
        // Therefore we just replace the "middle" part in the original field name with the "retype-name" value
        String targetNameAttribute = target.getAttribute(DialogConstants.PN_NAME);
        String targetNamePart = NamingUtil.getValidFieldName(StringUtils.defaultIfEmpty(source.adaptTo(DialogField.class).name(), source.getName()));
        String retypeNamePart = NamingUtil.getValidFieldName(password.retype());
        // We deliberately use "targetName + postfix" ligament to minimize probability of "targetName" occurring
        // in the complete field name more than once
        String retypeName = targetNameAttribute.replace(
            targetNamePart + target.getNamePostfix(),
            retypeNamePart + target.getNamePostfix());
        target.attribute(DialogConstants.PN_RETYPE, retypeName);
    }
}
