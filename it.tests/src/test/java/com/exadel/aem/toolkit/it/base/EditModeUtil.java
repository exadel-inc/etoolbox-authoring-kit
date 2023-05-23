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
package com.exadel.aem.toolkit.it.base;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import com.exadel.aem.toolkit.core.CoreConstants;

/**
 * Contains utility methods for managing AEM edit mode in-page elements
 */
public class EditModeUtil {

    private static final String SELECTOR_TOOLBAR = "#EditableToolbar";

    /**
     * Default (instantiation-blocking) constructor
     */
    private EditModeUtil() {
    }

    /**
     * Opens the component dialog for a component with the given path
     * @param path Component node address as specified by the {@code [data-path='...']} component attribute
     * @return The dialog object
     */
    public static SelenideElement openComponentDialog(String path) {
        return openComponentDialog(path, "CONFIGURE");
    }

    /**
     * Opens the component dialog for a component with the given path by executing the provided {@code action}
     * @param path   Component node address as specified by the {@code [data-path='...']} component attribute
     * @param action Action (usually manifested by a floating toolbar button) used to call in the dialog
     * @return The dialog object
     */
    public static SelenideElement openComponentDialog(String path, String action) {
        String operator = StringUtils.startsWith(path, CoreConstants.WILDCARD) ? "$=" : CoreConstants.EQUALITY_SIGN;
        String selector = "[data-path" + operator + "'" + StringUtils.strip(path, CoreConstants.WILDCARD) + "']";
        Selenide.$(selector).click();

        Selenide.$(SELECTOR_TOOLBAR).should(Condition.appear);
        SelenideElement configureButton = Selenide.$("button[data-action='" + action + "']");
        configureButton.click();

        SelenideElement componentDialog = Selenide.$("coral-dialog[open]");
        componentDialog.should(Condition.appear);
        return componentDialog;
    }

    /**
     * Performs the "save" command on the given dialog
     * @param dialog Dialog entity
     */
    public static void saveComponentDialog(SelenideElement dialog) {
        dialog.$("button.cq-dialog-submit").click();
    }

    /**
     * Performs the "cancel" command on the given dialog
     * @param dialog Dialog entity
     */
    public static void cancelComponentDialog(SelenideElement dialog) {
        if (dialog == null) {
            return;
        }
        dialog.$("button.cq-dialog-cancel").click();
    }

    /**
     * Call the "Insert new component" dialog for a container with the given path. Returns the list of insertion options
     * the dialog contains
     * @param path Component node address as specified by the {@code [data-path='...']} container attribute
     * @return Collection of dialog elements representing the insertion options
     */
    public static ElementsCollection getInsertChildOptions(String path) {
        return getInsertChildOptions(Selenide.$("[data-path='" + path + "']"));
    }

    /**
     * Call the "Insert new component" dialog for a container with the given UI overlay
     * the dialog contains
     * @param clickableOverlay The UI element that overlays the target container
     * @return Collection of dialog elements representing the insertion options
     */
    public static ElementsCollection getInsertChildOptions(SelenideElement clickableOverlay) {
        clickableOverlay.click();
        Selenide.$(SELECTOR_TOOLBAR).should(Condition.appear);
        Selenide.$("[data-action='INSERT']").click();
        SelenideElement insertDialogHeader = Selenide.element(By.xpath("//*[text()='Insert New Component']"));
        insertDialogHeader.should(Condition.appear);
        return insertDialogHeader.ancestor("coral-dialog").$$("coral-selectlist-item");
    }
}
