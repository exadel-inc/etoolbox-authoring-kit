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

package com.exadel.aem.toolkit.samples.models;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditorType;
import com.exadel.aem.toolkit.api.annotations.editconfig.InplaceEditingConfig;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.annotations.layouts.Tabs;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.PathField;
import com.exadel.aem.toolkit.api.annotations.widgets.Switch;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Attribute;
import com.exadel.aem.toolkit.samples.constants.GroupConstants;
import com.exadel.aem.toolkit.samples.constants.PathConstants;

@AemComponent(
    path = "content/warrior-component",
    title = "Warrior Component",
    description = "Create your own warrior",
    resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
    componentGroup = GroupConstants.COMPONENT_CONTAINER_GROUP,
    isContainer = true
)
@Dialog
@EditConfig(
    inplaceEditing = {
        @InplaceEditingConfig(
            title = "Component title",
            propertyName = "title",
            type = EditorType.PLAINTEXT,
            editElementQuery = ".warrior-component-title"
        ),
        @InplaceEditingConfig(
            title = "Warrior's name",
            propertyName = "name",
            type = EditorType.PLAINTEXT,
            editElementQuery = ".warrior-name-span"
        )
    }
)
@ChildEditConfig(
    actions = "copymove"
)
@Tabs(
    value = {
        @Tab(title = WarriorComponent.TAB_MAIN),
        @Tab(title = WarriorComponent.TAB_PHOTO),
        @Tab(title = WarriorComponent.TAB_THEME)
    }
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class WarriorComponent {

    static final String TAB_MAIN = "Main info";
    static final String TAB_PHOTO = "Photo";
    static final String TAB_THEME = "Color theme";
    static final String DEFAULT_NAME = "The Guy";

    private static final String LABEL_TITLE = "Container title";
    private static final String LABEL_NAME = "Warrior name";
    private static final String LABEL_PHOTO = "Photo of warrior";
    private static final String LABEL_COLOR_THEME = "Dark theme";

    private static final String DEFAULT_TITLE = "Make your warrior";
    private static final String DARK_THEME_CLASS = "dark-theme";
    private static final String LIGHT_THEME_CLASS = "light-theme";

    @DialogField(label = LABEL_TITLE)
    @TextField(emptyText = DEFAULT_TITLE)
    @Attribute(className = "test")
    @ValueMapValue
    private String title;

    @DialogField(label = LABEL_NAME)
    @TextField(emptyText = WarriorComponent.DEFAULT_NAME)
    @ValueMapValue
    private String name;

    @DialogField(label = LABEL_PHOTO)
    @PathField(
        emptyText = "Face",
        rootPath = "/content/dam"
    )
    @Place(WarriorComponent.TAB_PHOTO)
    @ValueMapValue
    private String photo;

    @DialogField(label = LABEL_COLOR_THEME)
    @Switch
    @Place(WarriorComponent.TAB_THEME)
    @Default(booleanValues = false)
    @ValueMapValue
    private boolean colorTheme;

    public String getTitle() {
        return StringUtils.defaultIfBlank(title, DEFAULT_TITLE);
    }

    public String getName() {
        return StringUtils.defaultIfBlank(name, DEFAULT_NAME);
    }

    public String getPhoto() {
        return photo;
    }

    public String getColorTheme() {
        return colorTheme ? DARK_THEME_CLASS : LIGHT_THEME_CLASS;
    }
}
