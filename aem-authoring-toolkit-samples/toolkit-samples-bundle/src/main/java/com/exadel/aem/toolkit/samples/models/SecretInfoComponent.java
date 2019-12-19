package com.exadel.aem.toolkit.samples.models;

import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Password;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.textarea.TextArea;
import com.exadel.aem.toolkit.samples.constants.PathConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Dialog(
        name = "content/secret-info-component",
        title = "Secret Info",
        description = "Secret info about the warrior",
        resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
        componentGroup = "Toolkit Samples",
        tabs= {
                @Tab(title = SecretInfoComponent.TAB_PASSWORD),
                @Tab(title = SecretInfoComponent.TAB_SECRET),
        }
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class SecretInfoComponent {

        public static final String TAB_PASSWORD = "Password";
        public static final String TAB_SECRET = "Secret";

        @PlaceOnTab(SecretInfoComponent.TAB_PASSWORD)
        @Password(retype = "confirmPassword")
        @DialogField(
                description = "Enter a password to unlock secret information about the warrior",
                label = "Enter secret password"
        )
        @ValueMapValue
        private String password;

        @PlaceOnTab(SecretInfoComponent.TAB_PASSWORD)
        @Password
        @DialogField(label = "Confirm your password")
        @ValueMapValue
        private String confirmPassword;

        @PlaceOnTab(SecretInfoComponent.TAB_SECRET)
        @TextArea(autofocus = true, maxlength = 200)
        @DialogField(
                label = "Secret info",
                name = "./info",
                description = "Enter secret information about your warrior"
        )
        @ValueMapValue
        private String info;

        public String getPassword() { return password; }

        public String getInfo() {
                if (info != null && !"".equals(info)) {
                        return info;
                }
                return "This warrior is clear";
        }
}
