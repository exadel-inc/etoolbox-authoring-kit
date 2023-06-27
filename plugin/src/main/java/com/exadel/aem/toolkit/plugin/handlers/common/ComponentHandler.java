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
package com.exadel.aem.toolkit.plugin.handlers.common;

import java.lang.annotation.Annotation;
import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.annotations.RenderingFilter;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.ScopeUtil;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with common properties originating from a
 * {@link Source} object that define an AEM component
 */
public class ComponentHandler implements BiConsumer<Source, Target> {

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        Annotation annotation = source.adaptTo(AemComponent.class);
        if (annotation == null) {
            annotation = source.adaptTo(Dialog.class);
        }
        target
            .attribute(DialogConstants.PN_PRIMARY_TYPE, DialogConstants.NT_COMPONENT)
            .attributes(
                annotation,
                new RenderingFilter(annotation).and(member -> ScopeUtil.fits(Scopes.COMPONENT, member)));
    }
}
