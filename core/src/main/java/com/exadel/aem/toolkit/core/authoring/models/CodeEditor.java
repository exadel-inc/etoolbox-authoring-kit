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
package com.exadel.aem.toolkit.core.authoring.models;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.main.AemComponent;

/**
 * Extends {@link BaseModel} to provide additional facilities for rendering {@link
 * com.exadel.aem.toolkit.api.annotations.widgets.codeeditor.CodeEditor}-defined widgets in Granite UI
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@AemComponent(
    path = "components/authoring/codeeditor",
    title = "CodeEditor",
    resourceSuperType = "etoolbox-authoring-kit/components/authoring/base"
)
public class CodeEditor extends BaseModel {

    private static final String DEFAULT_THEME = "crimson_editor";
    private static final String DEFAULT_MODE = "json";

    @ValueMapValue
    private String source;

    @ValueMapValue
    @Default(values = DEFAULT_THEME)
    private String theme;

    @ValueMapValue
    @Default(values = DEFAULT_MODE)
    private String mode;

    @ValueMapValue
    private String options;

    @ValueMapValue
    private String dataPrefix;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue() {
        String result = ObjectUtils.defaultIfNull(super.getValue(), StringUtils.EMPTY).toString();
        if (StringUtils.isNotBlank(dataPrefix) && StringUtils.startsWith(result, dataPrefix)) {
            return result.substring(dataPrefix.length());
        }
        return result;
    }

    /**
     * Retrieves the {@code source} value as defined by the user for the current component
     * @return A nullable string value
     * @see com.exadel.aem.toolkit.api.annotations.widgets.codeeditor.CodeEditor#source()
     */
    public String getSource() {
        return source;
    }

    /**
     * Retrieves the {@code theme} value as defined by the user for the current component
     * @return A nullable string value
     * @see com.exadel.aem.toolkit.api.annotations.widgets.codeeditor.CodeEditor#theme()
     */
    public String getTheme() {
        return theme;
    }

    /**
     * Retrieves the language {@code mode} value as defined by the user for the current component
     * @return A nullable string value; can be empty
     * @see com.exadel.aem.toolkit.api.annotations.widgets.codeeditor.CodeEditor#mode()
     */
    public String getMode() {
        return mode;
    }

    /**
     * Retrieves initialization options for the current component
     * @return A nullable string value
     * @see com.exadel.aem.toolkit.api.annotations.widgets.codeeditor.CodeEditor#options()
     */
    public String getOptions() {
        return options;
    }

    /**
     * Retrieves the {@code dataPrefix} value as defined by the user for the current component
     * @return A nullable string value
     * @see com.exadel.aem.toolkit.api.annotations.widgets.codeeditor.CodeEditor#dataPrefix() ()
     */
    public String getDataPrefix() {
        return dataPrefix;
    }
}
