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
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Password;
import com.exadel.aem.toolkit.api.annotations.widgets.button.Button;
import com.exadel.aem.toolkit.api.annotations.widgets.button.ButtonType;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.samples.constants.GroupConstants;
import com.exadel.aem.toolkit.samples.constants.PathConstants;

@AemComponent(
    path = "content/secret-info-component",
    title = "Secret Info Component",
    description = "Secret info about the warrior",
    resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
    componentGroup = GroupConstants.COMPONENT_GROUP
)
@Dialog
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class SecretInfoComponent {

    static final String TAB_PASSWORD = "Password";
    static final String TAB_SECRET = "Secret";

    private static final String LABEL_PASSWORD = "Enter password";
    private static final String DESCRIPTION_PASSWORD = "Enter a password to unlock secret information about the warrior";

    private static final String LABEL_CONFIRM_PASSWORD = "Confirm your password";

    private static final String LABEL_INFO = "Secret info";
    private static final String DESCRIPTION_INFO = "Enter secret information about your warrior";

    private static final String DEFAULT_SECRET = "This warrior has no secrets";

    @DialogField(
        label = LABEL_PASSWORD,
        description = DESCRIPTION_PASSWORD
    )
    @Password(retype = "confirmPassword")
    @Place(SecretInfoComponent.TAB_PASSWORD)
    @ValueMapValue
    private String password;

    @DialogField(label = LABEL_CONFIRM_PASSWORD)
    @Password
    @Place(SecretInfoComponent.TAB_PASSWORD)
    @ValueMapValue
    private String confirmPassword;

    @DialogField(
        label = LABEL_INFO,
        description = DESCRIPTION_INFO
    )
    @TextArea(autofocus = true, maxlength = 200)
    @Place(SecretInfoComponent.TAB_SECRET)
    @ValueMapValue
    private String info;

    @DialogField
    @Button(
        type = ButtonType.SUBMIT,
        text = "Save",
        icon = "edit",
        command = "shift+s",
        block = true
    )
    @Place(SecretInfoComponent.TAB_SECRET)
    private String button;

    public String getPassword() {
        return password;
    }

    public String getInfo() {
        return StringUtils.defaultIfBlank(info, DEFAULT_SECRET);
    }
}
