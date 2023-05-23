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
package com.exadel.aem.toolkit.it;

import org.junit.BeforeClass;
import org.junit.Test;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.it.base.AemConnection;
import com.exadel.aem.toolkit.it.base.EditModeUtil;
import com.exadel.aem.toolkit.it.base.Render;
import com.exadel.aem.toolkit.it.cases.ignorefreshness.IgnoreFreshnessComponent;
import com.exadel.aem.toolkit.it.cases.ignorefreshness.NoIgnoreFreshnessComponent;

@Render({
    IgnoreFreshnessComponent.class,
    NoIgnoreFreshnessComponent.class
})
public class IgnoreFreshnessTest {

    private static final String SELECTOR_CHECKED_RADIO = "[name='./selection1'][checked] input";
    private static final String SELECTOR_SELECTED_OPTION = "coral-select[name='./selection2'] coral-select-item[selected]";
    private static final String SELECTED_VALUE = "2";

    @BeforeClass
    public static void login() {
        AemConnection.login();
        AemConnection.open("/editor.html/content/etoolbox-authoring-kit-test/ignoreFreshness.html");
    }

    @Test
    public void shouldHandleIgnoreFreshness() {
        SelenideElement dialog = EditModeUtil.openComponentDialog("/content/etoolbox-authoring-kit-test/ignoreFreshness/jcr:content/root/grid_0/ignoring");

        SelenideElement checkedRadioButton = dialog.$(SELECTOR_CHECKED_RADIO);
        checkedRadioButton.should(Condition.exist);
        checkedRadioButton.shouldHave(Condition.attribute(CoreConstants.PN_VALUE, SELECTED_VALUE));

        SelenideElement selectedOption = dialog.$(SELECTOR_SELECTED_OPTION);
        selectedOption.should(Condition.exist);
        selectedOption.shouldHave(Condition.attribute(CoreConstants.PN_VALUE, SELECTED_VALUE));

        EditModeUtil.cancelComponentDialog(dialog);
    }

    @Test
    public void shouldOmitIgnoreFreshness() {
        SelenideElement dialog = EditModeUtil.openComponentDialog("/content/etoolbox-authoring-kit-test/ignoreFreshness/jcr:content/root/grid_0/non-ignoring");

        SelenideElement checkedRadioButton = dialog.$(SELECTOR_CHECKED_RADIO);
        checkedRadioButton.shouldNot(Condition.exist);

        SelenideElement selectedOption = dialog.$(SELECTOR_SELECTED_OPTION);
        selectedOption.shouldHave(Condition.attribute(CoreConstants.PN_VALUE, "1"));

        EditModeUtil.cancelComponentDialog(dialog);
    }
}
