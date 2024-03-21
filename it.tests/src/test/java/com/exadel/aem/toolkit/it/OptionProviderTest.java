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

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.it.base.AemConnection;
import com.exadel.aem.toolkit.it.base.EditModeUtil;
import com.exadel.aem.toolkit.it.base.Render;
import com.exadel.aem.toolkit.it.cases.optionprovider.OptionsComponent;

@Render(OptionsComponent.class)
public class OptionProviderTest {

    private static final String SELECTOR_MULTISOURCE_ITEM = "coral-select[name='./multisource'] coral-select-item";

    private SelenideElement dialog;

    @BeforeClass
    public static void login() {
        Configuration.timeout = AemConnection.TIMEOUT;
        Configuration.pollingInterval = AemConnection.POLLING_INTERVAL;
        AemConnection.login();
    }

    @Before
    public void openDialog() {
        AemConnection.open("/editor.html/content/etoolbox-authoring-kit-test/optionProvider.html");
        dialog = EditModeUtil.openComponentDialog("/content/etoolbox-authoring-kit-test/optionProvider/jcr:content/root/grid_0/options");
    }

    @Test
    public void shouldRenderFromSimpleList() {
        ElementsCollection greetingOptions = dialog.$$("coral-select[name='./greeting'] coral-select-item");
        String[] greetingValues = greetingOptions
            .asFixedIterable()
            .stream()
            .map(option -> option.getAttribute("value"))
            .toArray(String[]::new);
        Assert.assertArrayEquals(new String[]{"none", "en", "ja", "zh", "ww"}, greetingValues);
        SelenideElement selectedGreetingOption = dialog.$("coral-select[name='./greeting'] coral-select-item[selected]");
        selectedGreetingOption.shouldHave(Condition.attribute("value", "en"));
    }

    @Test
    public void shouldRenderFromCustomList() {
        ElementsCollection socialChannelsOptions = dialog.$$("coral-radio[name='./socialChannel'] input");
        String[] socialChannelsValues = socialChannelsOptions
            .asFixedIterable()
            .stream()
            .map(option -> option.getAttribute(CoreConstants.PN_VALUE))
            .toArray(String[]::new);
        Assert.assertArrayEquals(new String[]{"none", "linkedin", "myspace", "rss"}, socialChannelsValues);
        SelenideElement checkedSocialChannelOption = dialog.$("coral-radio[name='./socialChannel'][checked] input");
        checkedSocialChannelOption.shouldHave(Condition.attribute(CoreConstants.PN_VALUE, "rss"));
    }

    @Test
    public void shouldRenderFromNode() {
        ElementsCollection pageOptions = dialog.$$("coral-select[name='./page'] coral-select-item");
        String[] pageLabels = pageOptions
            .asFixedIterable()
            .stream()
            .map(SelenideElement::getOwnText)
            .toArray(String[]::new);
        Assert.assertArrayEquals(new String[]{"Nested-Container", "Empty"}, pageLabels);
    }

    @Test
    public void shouldRenderFromTagFolder() {
        ElementsCollection fruitOptions = dialog.$$("coral-radio[name='./fruit'] input");
        String[] fruitValues = fruitOptions
            .asFixedIterable()
            .stream()
            .map(option -> option.getAttribute(CoreConstants.PN_VALUE))
            .toArray(String[]::new);
        Assert.assertArrayEquals(new String[]{"fruit:apple", "fruit:banana", "fruit:orange"}, fruitValues);
        SelenideElement checkedFruitOption = dialog.$("coral-radio[name='./fruit'][checked] input");
        checkedFruitOption.shouldHave(Condition.attribute(CoreConstants.PN_VALUE, "fruit:banana"));
    }

    @Test
    public void shouldRenderFromEnum() {
        ElementsCollection colorOptions = dialog.$$("coral-select[name='./color1'] coral-select-item");
        String[] colorLabels = colorOptions
            .asFixedIterable()
            .stream()
            .map(SelenideElement::getOwnText)
            .toArray(String[]::new);
        Assert.assertArrayEquals(
            new String[]{"None", "Blue", "Green", "Indigo", "Orange", "Red", "Violet", "Yellow"},
            colorLabels);
        SelenideElement selectedColorOption = dialog.$("coral-select[name='./color1'] coral-select-item[selected]");
        Assert.assertEquals("Orange", selectedColorOption.getOwnText());
    }

    @Test
    public void shouldRenderFromConstantsClass() {
        ElementsCollection colorOptions = dialog.$$("coral-radio[name='./color2'] input");
        String[] colorValues = colorOptions
            .asFixedIterable()
            .stream()
            .map(option -> option.getAttribute(CoreConstants.PN_VALUE))
            .toArray(String[]::new);
        Assert.assertArrayEquals(new String[]{"#FF0000", "#00FF00", "#0000FF", "#000000"}, colorValues);
    }

    @Test
    public void shouldRenderFromJsonEndpoint() {
        ElementsCollection userOptions = dialog.$$("coral-select[name='./user'] coral-select-item");
        userOptions.shouldBe(CollectionCondition.size(10));
        boolean allEmails = userOptions.asFixedIterable().stream().allMatch(option -> StringUtils.contains(option.getAttribute(CoreConstants.PN_VALUE), CoreConstants.SEPARATOR_AT));
        Assert.assertTrue(allEmails);
    }

    @Test
    public void shouldRenderLoadedAsync() {
        ElementsCollection multisourceSelectionOptions = dialog.$$("coral-radio[name='./multisourceSelection']");
        multisourceSelectionOptions.shouldBe(CollectionCondition.size(3));

        multisourceSelectionOptions.get(0).click();
        Selenide.Wait()
            .until(webDriver ->
                dialog.$$(SELECTOR_MULTISOURCE_ITEM).shouldBe(CollectionCondition.size(3)));
        String[] optionValues = dialog.$$(SELECTOR_MULTISOURCE_ITEM)
            .asFixedIterable()
            .stream()
            .map(element -> element.getAttribute(CoreConstants.PN_VALUE))
            .toArray(String[]::new);
        Assert.assertArrayEquals(new String[]{"carrot", "lettuce", "potato"}, optionValues);

        multisourceSelectionOptions.get(1).click();
        Selenide.Wait()
            .until(webDriver ->
                dialog.$$(SELECTOR_MULTISOURCE_ITEM).shouldBe(CollectionCondition.size(0)));

        multisourceSelectionOptions.get(2).click();
        Selenide.Wait()
            .until(webDriver ->
                dialog.$$(SELECTOR_MULTISOURCE_ITEM).shouldBe(CollectionCondition.size(3)));

        optionValues = dialog.$$(SELECTOR_MULTISOURCE_ITEM)
            .asFixedIterable()
            .stream()
            .map(element -> element.getAttribute(CoreConstants.PN_VALUE))
            .toArray(String[]::new);
        Assert.assertArrayEquals(new String[]{"apple", "banana", "orange"}, optionValues);
    }

    @After
    public void closeDialog() {
        EditModeUtil.cancelComponentDialog(dialog);
    }
}
