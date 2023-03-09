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
package com.exadel.aem.toolkit.plugin.handlers.widgets.common;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.imageupload.ImageUpload;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.adapters.PluginsSetting;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.TargetUtil;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties that provide the implementation
 * of authoring assistants
 */
@Handles({RichTextEditor.class, TextField.class, TextArea.class, ImageUpload.class})
public class AssistantPluginHandler implements Handler {

    private static final String PLUGIN_TOKEN = "assistant";
    private static final String CLIENTLIB_CATEGORY_ASSISTANT = "eak.authoring.assistant";
    private static final String SUFFIX_ASSISTANT_SETTINGS = "_eakAssistantSettings";

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided
     * {@code Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        boolean pluginPresent = false;
        if (source.adaptTo(RichTextEditor.class) != null) {
            pluginPresent = ArrayUtils.contains(
                source.adaptTo(RichTextEditor.class).features(),
                RteFeatures.Popovers.ASSISTANT);
        } else {
            pluginPresent = ArrayUtils.contains(
                source.adaptTo(PluginsSetting.class).getValue(),
                PLUGIN_TOKEN);
        }
        if (!pluginPresent) {
            return;
        }
        target
            .getParent()
            .getOrCreateTarget(target.getName() + SUFFIX_ASSISTANT_SETTINGS)
            .attribute(DialogConstants.PN_NAME, getSettingFieldName(target.getAttribute(DialogConstants.PN_NAME)))
            .attribute(DialogConstants.PN_SLING_RESOURCE_TYPE, ResourceTypes.HIDDEN);
        TargetUtil.populateClientLibrary(target, CLIENTLIB_CATEGORY_ASSISTANT);
    }

    /**
     * Called by {@link AssistantPluginHandler#accept(Source, Target)} to determine the {@code name} attribute for the field
     * containing assistant settings for the current dialog field. The routine considers whether the current dialog
     * field name ends with a slash to avoid an ugly settings field's name
     * @param source The name of the field for which settings are being stored
     * @return The name of the settings field
     */
    private static String getSettingFieldName(String source) {
        if (StringUtils.endsWith(source, CoreConstants.SEPARATOR_SLASH)) {
            return source + StringUtils.strip(SUFFIX_ASSISTANT_SETTINGS, CoreConstants.SEPARATOR_UNDERSCORE);
        }
        return source + SUFFIX_ASSISTANT_SETTINGS;
    }
}
