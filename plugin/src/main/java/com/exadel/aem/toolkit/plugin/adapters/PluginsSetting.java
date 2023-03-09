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
package com.exadel.aem.toolkit.plugin.adapters;

import org.apache.commons.lang3.ArrayUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.imageupload.ImageUpload;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.api.handlers.Adapts;
import com.exadel.aem.toolkit.api.handlers.Source;

/**
 * Adapts a {@link Source} object to manage the associated plugin data derived from source annotations
 */
@Adapts(Source.class)
public class PluginsSetting {

    private final String[] plugins;

    /**
     * Instance constructor per the {@link Adapts} contract
     * @param source {@code Source} object that will be used for extracting plugins data
     */
    public PluginsSetting(Source source) {
        plugins = extractPluginsData(source);
    }

    /**
     * Retrieves the plugins associated with the current {@code Source}
     * @return A non-null array of strings; can be empty
     */
    public String[] getValue() {
        return plugins;
    }

    /**
     * Checks the given {@code Source} on whether it contains a {@code plugins} setting and returns the value. Returns
     * an empty array otherwise
     * @param source {@code Source} object that will be used for extracting plugins data
     * @return A non-null array of strings; can be empty
     */
    private static String[] extractPluginsData(Source source) {
        if (source.adaptTo(TextField.class) != null) {
            return source.adaptTo(TextField.class).plugins();
        }
        if (source.adaptTo(TextArea.class) != null) {
            return source.adaptTo(TextArea.class).plugins();
        }
        if (source.adaptTo(ImageUpload.class) != null) {
            return source.adaptTo(ImageUpload.class).plugins();
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
