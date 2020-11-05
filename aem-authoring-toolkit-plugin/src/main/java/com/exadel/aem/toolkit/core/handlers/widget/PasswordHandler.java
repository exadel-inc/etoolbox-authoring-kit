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

import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;

import com.exadel.aem.toolkit.api.annotations.widgets.Password;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.NamingUtil;

/**
 * {@link Handler} implementation used to create markup responsible for {@code Password} widget functionality
 * within the {@code cq:dialog} XML node
 */
class PasswordHandler implements Handler, BiConsumer<Source, Target> {
    /**
     * Processes the user-defined data and writes it to XML entity
     * @param source Current {@code SourceFacade} instance
     * @param target Current XML targetFacade
     */
    @Override
    public void accept(Source source, Target target) {
        Password password = source.adaptTo(Password.class);
        if(!password.retype().isEmpty()) {
            target.attribute(DialogConstants.PN_RETYPE,
                    source.fromValueMap(DialogConstants.PN_PREFIX) +
                            NamingUtil.getValidFieldName(password.retype()) +
                            source.fromValueMap(DialogConstants.PN_POSTFIX));
        }
    }
}
