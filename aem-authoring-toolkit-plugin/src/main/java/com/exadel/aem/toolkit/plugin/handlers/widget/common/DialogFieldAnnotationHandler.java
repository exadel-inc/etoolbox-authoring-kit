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

package com.exadel.aem.toolkit.plugin.handlers.widget.common;

import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.util.DialogConstants;
import com.exadel.aem.toolkit.plugin.util.PluginNamingUtility;

/**
 * Handler for storing {@link DialogField} properties to a Granite UI widget node
 */
public class DialogFieldAnnotationHandler implements BiConsumer<Source, Target> {

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
        String name = PluginNamingUtility.stripGetterPrefix(source.getName());
        if (StringUtils.isNotBlank(dialogField.name())) {
            name = !DialogConstants.PATH_SEPARATOR.equals(dialogField.name()) && !DialogConstants.RELATIVE_PATH_PREFIX.equals(dialogField.name())
                ? PluginNamingUtility.getValidFieldName(dialogField.name())
                : DialogConstants.RELATIVE_PATH_PREFIX;
        }
        String prefix = target.getNamePrefix();

        // In case there are multiple sources in multifield container, their "name" values must not be preceded
        // with "./" which is by default
        // see https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/reference-materials/granite-ui/api/jcr_root/libs/granite/ui/components/coral/foundation/form/multifield/index.html#examples
        // That is why we must alter the default name prefix for the ongoing set of sources
        Target multifieldAncestor = target.findAncestor(t -> ResourceTypes.MULTIFIELD.equals(t.getAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE)));
        if (multifieldAncestor == null || multifieldAncestor.equals(target.getParent())) {
            prefix = DialogConstants.RELATIVE_PATH_PREFIX + prefix;
        }

        if (StringUtils.isNotBlank(prefix)
                && !(prefix.equals(DialogConstants.RELATIVE_PATH_PREFIX) && name.equals(DialogConstants.RELATIVE_PATH_PREFIX))
                && !(prefix.equals(DialogConstants.RELATIVE_PATH_PREFIX) && name.startsWith(DialogConstants.PARENT_PATH_PREFIX))) {
            name = prefix + name;
        }
        name = name + target.getNamePostfix();
        target.attribute(DialogConstants.PN_NAME, name);
    }
}
