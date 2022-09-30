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
package com.exadel.aem.toolkit.plugin.handlers;

import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.handlers.Handler;
import com.exadel.aem.toolkit.api.handlers.Handles;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.utils.TargetUtil;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance with properties that provide the implementation
 * of {@code Writesonic Bridge}
 */
@Handles({TextField.class, RichTextEditor.class})
public class WritesonicHandler implements Handler {

    private static final String TOKEN_WRITESOMIC = "writesonic";
    private static final String CLIENTLIB_CATEGORY_WRITESONIC = "eak.authoring.writesonic-bridge";

    /**
     * Processes data that can be extracted from the given {@code Source} and stores it into the provided {@code
     * Target}
     * @param source {@code Source} object used for data retrieval
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        RichTextEditor richTextEditor = source.adaptTo(RichTextEditor.class);
        TextField textField = source.adaptTo(TextField.class);
        boolean pluginPresent = false;
        if (richTextEditor != null) {
            pluginPresent = ArrayUtils.contains(richTextEditor.features(), RteFeatures.Popovers.WRITESONIC);
        } else if (textField != null) {
            pluginPresent = ArrayUtils.contains(textField.plugins(), TOKEN_WRITESOMIC);
        }
        if (pluginPresent) {
            TargetUtil.populateClientLibrary(target, CLIENTLIB_CATEGORY_WRITESONIC);
        }
    }
}
