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

import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import com.google.common.collect.ImmutableMap;
import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.it.base.AemConnection;
import com.exadel.aem.toolkit.it.base.EditModeUtil;
import com.exadel.aem.toolkit.it.base.Order;
import com.exadel.aem.toolkit.it.base.Render;
import com.exadel.aem.toolkit.it.cases.Constants;
import com.exadel.aem.toolkit.it.cases.lists.SocialChannelDialog;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

@Render(SocialChannelDialog.class)
public class ListsTest {

    private static final String SELECTOR_ITEM = ".listItem";
    private static final String SELECTOR_OVERLAY = ".cq-Overlay--component:not(.cq-Overlay--container)";

    private static final String ACTION_CONFIGURE = "LIST_ITEM_CONFIGURE";

    private static final String SELECTOR_FIELD_ICON = "[name='./icon']";
    private static final String SELECTOR_FIELD_ID = "[name='./id']";
    private static final String SELECTOR_FIELD_JCR_TITLE = "[name='./jcr:title']";
    private static final String SELECTOR_FIELD_PAGE_NAME = "[name='pageName']";
    private static final String SELECTOR_FIELD_TITLE = "[name='./title']";
    private static final String SELECTOR_FIELD_URL = "[name='./url']";
    private static final String SELECTOR_FIELD_VALUE = "[name='./value']";

    private static final String TITLE_CIAO = "Ciao";
    private static final String VALUE_IT = "it";
    private static final String ID_LINKEDIN = "linkedin";
    private static final String ICON_LINKEDIN = "https://upload.wikimedia.org/wikipedia/commons/0/01/LinkedIn_Logo.svg";
    private static final String ID_TEST = "test";
    private static final String TITLE_TEST = "Test";
    private static final String URL_TEST = "//www.acme.com/social";
    private static final String ICON_TEST = "//www.acme.com/favicon.ico";

    @BeforeClass
    public static void login() {
        Configuration.timeout = AemConnection.TIMEOUT;
        Configuration.pollingInterval = AemConnection.POLLING_INTERVAL;
        AemConnection.login();
    }

    @Test
    @Order(0)
    public void shouldDisplayStandardListDialog() {
        displayDialog(
            "editor.html/content/etoolbox-authoring-kit-test/lists/greetings.html",
            editDialog -> {
                Assert.assertEquals("Hello", editDialog.$(SELECTOR_FIELD_JCR_TITLE).getValue());
                Assert.assertEquals("en", editDialog.$(SELECTOR_FIELD_VALUE).getValue());
            },
            editDialog -> {
                Assert.assertEquals(StringUtils.EMPTY,  editDialog.$(SELECTOR_FIELD_JCR_TITLE).getValue());
                Assert.assertEquals(StringUtils.EMPTY, editDialog.$(SELECTOR_FIELD_VALUE).getValue());
                populateDialogFields(editDialog, ImmutableMap.of(
                    SELECTOR_FIELD_JCR_TITLE, TITLE_CIAO,
                    SELECTOR_FIELD_VALUE, VALUE_IT));
            });
    }

    @Test
    @Order(1)
    public void shouldDisplayStandardList() {
        AemConnection.open("content/etoolbox-authoring-kit-test/lists/greetings.html");
        ElementsCollection listItems = Selenide.$$(SELECTOR_ITEM);
        listItems.shouldBe(CollectionCondition.size(4));

        Assert.assertEquals("Hello", getListItemProperty(listItems.get(0), DialogConstants.PN_JCR_TITLE));
        Assert.assertEquals("en", getListItemProperty(listItems.get(0), CoreConstants.PN_VALUE));

        Assert.assertEquals("Hola", getListItemProperty(listItems.get(1), DialogConstants.PN_JCR_TITLE));
        Assert.assertEquals("es", getListItemProperty(listItems.get(1), CoreConstants.PN_VALUE));

        Assert.assertEquals("こんにちは", getListItemProperty(listItems.get(2), DialogConstants.PN_JCR_TITLE));
        Assert.assertEquals("ja", getListItemProperty(listItems.get(2), CoreConstants.PN_VALUE));

        Assert.assertEquals(TITLE_CIAO, getListItemProperty(listItems.get(3), DialogConstants.PN_JCR_TITLE));
        Assert.assertEquals(VALUE_IT, getListItemProperty(listItems.get(3), CoreConstants.PN_VALUE));
    }

    @Test
    @Order(2)
    public void shouldDisplayCustomizedListDialog() {
        displayDialog(
            "editor.html/content/etoolbox-authoring-kit-test/lists/social-channels.html",
            editDialog -> {
                Assert.assertEquals(ID_LINKEDIN, editDialog.$(SELECTOR_FIELD_ID).getValue());
                Assert.assertEquals("Linkedin", editDialog.$(SELECTOR_FIELD_TITLE).getValue());
                Assert.assertEquals(
                    "//www.linkedin.com/sharing",
                    editDialog.$(SELECTOR_FIELD_URL).getValue());
                Assert.assertEquals(
                    ICON_LINKEDIN,
                    editDialog.$(SELECTOR_FIELD_ICON).getValue());

            },
            editDialog -> {
                Assert.assertEquals(StringUtils.EMPTY,  editDialog.$(SELECTOR_FIELD_ID).getValue());
                Assert.assertEquals(StringUtils.EMPTY, editDialog.$(SELECTOR_FIELD_TITLE).getValue());
                Assert.assertEquals(StringUtils.EMPTY,  editDialog.$(SELECTOR_FIELD_URL).getValue());
                Assert.assertEquals(StringUtils.EMPTY, editDialog.$(SELECTOR_FIELD_ICON).getValue());
                populateDialogFields(editDialog, ImmutableMap.of(
                    SELECTOR_FIELD_ID, ID_TEST,
                    SELECTOR_FIELD_TITLE, TITLE_TEST,
                    SELECTOR_FIELD_URL, URL_TEST,
                    SELECTOR_FIELD_ICON, ICON_TEST
                ));
            });
    }

    @Test
    @Order(3)
    public void shouldDisplayCustomizedList() {
        AemConnection.open("content/etoolbox-authoring-kit-test/lists/social-channels.html?wcmmode=disabled");
        ElementsCollection listItems = Selenide.$$(SELECTOR_ITEM);
        listItems.shouldBe(CollectionCondition.size(4));

        Assert.assertEquals(ID_LINKEDIN, getListItemProperty(listItems.get(0), "id"));
        listItems.get(0).$("img").shouldHave(Condition.attribute("src", ICON_LINKEDIN));
        Assert.assertEquals("//myspace.com/post", getListItemProperty(listItems.get(1), "url"));
        Assert.assertEquals("RSS", getListItemProperty(listItems.get(2), "title"));
        Assert.assertEquals("test", getListItemProperty(listItems.get(3), "id"));
        Assert.assertEquals("Test", getListItemProperty(listItems.get(3), "title"));
    }

    @Test
    @Order(4)
    public void shouldCreateNewSimpleList() {
        createNewList(
            "Greetings",
            "greetings2",
            null);
        displayDialog(
            "editor.html/content/etoolbox-authoring-kit-test/lists/greetings2.html",
            null,
            editDialog -> populateDialogFields(editDialog, ImmutableMap.of(
                SELECTOR_FIELD_JCR_TITLE, TITLE_CIAO,
                SELECTOR_FIELD_VALUE, VALUE_IT
            )));

        AemConnection.open("content/etoolbox-authoring-kit-test/lists/greetings2.html?wcmmode=disabled");
        ElementsCollection listItems = Selenide.$$(SELECTOR_ITEM);
        listItems.shouldBe(CollectionCondition.size(1));

        Assert.assertEquals(TITLE_CIAO, getListItemProperty(listItems.get(0), DialogConstants.PN_JCR_TITLE));
        Assert.assertEquals(VALUE_IT, getListItemProperty(listItems.get(0), CoreConstants.PN_VALUE));
    }

    @Test
    @Order(5)
    public void shouldCreateNewCustomizedList() {
        createNewList(
            "Social Channels",
            "social-channels2",
            Constants.JCR_COMPONENTS_ROOT + "/lists/socialChannel");
        displayDialog(
            "editor.html/content/etoolbox-authoring-kit-test/lists/social-channels2.html",
            null,
            editDialog -> populateDialogFields(editDialog, ImmutableMap.of(
                SELECTOR_FIELD_ID, ID_TEST,
                SELECTOR_FIELD_TITLE, TITLE_TEST,
                SELECTOR_FIELD_URL, URL_TEST,
                SELECTOR_FIELD_ICON, ICON_TEST
            )));

        AemConnection.open("content/etoolbox-authoring-kit-test/lists/social-channels2.html?wcmmode=disabled");
        ElementsCollection listItems = Selenide.$$(SELECTOR_ITEM);
        listItems.shouldBe(CollectionCondition.size(1));

        Assert.assertEquals(ID_TEST, getListItemProperty(listItems.get(0), "id"));
        Assert.assertEquals(TITLE_TEST, getListItemProperty(listItems.get(0), "title"));
        Assert.assertEquals(URL_TEST, getListItemProperty(listItems.get(0), "url"));
        Assert.assertEquals(ICON_TEST, getListItemProperty(listItems.get(0), "icon"));
    }

    /* ---------------
       Utility methods
       --------------- */

    private static void displayDialog(
        String pageAddress,
        Consumer<SelenideElement> existingItemProcessor,
        Consumer<SelenideElement> newItemProcessor) {
        SelenideElement listItemDialog;

        AemConnection.open(pageAddress);
        ElementsCollection overlays = Selenide.$$(SELECTOR_OVERLAY);
        if (existingItemProcessor != null) {
            overlays.shouldBe(CollectionCondition.size(4));

            String itemPath = overlays.first().getAttribute("data-path");
            listItemDialog = EditModeUtil.openComponentDialog(itemPath, ACTION_CONFIGURE);
            existingItemProcessor.accept(listItemDialog);
            EditModeUtil.cancelComponentDialog(listItemDialog);
        }

        EditModeUtil.getInsertChildOptions(overlays.last()).first().click();
        Selenide.Wait()
            .until((webDriver) ->
                Selenide.$$(SELECTOR_OVERLAY).size() == (existingItemProcessor != null ? 5 : 2));

        listItemDialog = EditModeUtil.openComponentDialog("*/listitem", ACTION_CONFIGURE);
        newItemProcessor.accept(listItemDialog);
        EditModeUtil.saveComponentDialog(listItemDialog);
        Selenide.refresh();
    }

    private static void populateDialogFields(SelenideElement dialog, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            SelenideElement input = dialog.$(entry.getKey());
            Selenide.Wait().until(ExpectedConditions.elementToBeClickable(input));
            input.click();
            input.setValue(entry.getValue());
        }
    }

    private static void createNewList(String title, String pageName, String itemType) {
        AemConnection.open("etoolbox-lists.html/content/etoolbox-authoring-kit-test/lists");
        Selenide.element(By.xpath("//coral-button-label[text()='Create']")).parent().click();
        Selenide.element(By.xpath("//coral-list-item-content[text()='List']")).ancestor("a").click();

        SelenideElement createListDialog = Selenide
            .element(By.xpath("//coral-dialog-header[text()='Create List']"))
            .ancestor("coral-dialog");
        createListDialog.should(Condition.appear);
        populateDialogFields(createListDialog, ImmutableMap.of(
            SELECTOR_FIELD_JCR_TITLE, title,
            SELECTOR_FIELD_PAGE_NAME, pageName));
        if (StringUtils.isNotEmpty(itemType)) {
            createListDialog.$("coral-select[name='./itemResourceType']").click();
            createListDialog.$("coral-selectlist-item[value='/apps/etoolbox-authoring-kit-test/components/content/lists/socialChannel']").click();
        }
        createListDialog.$("button[type='submit']").click();

        SelenideElement successDialog = Selenide.$(".coral3-Dialog--success[open]");
        successDialog.should(Condition.appear);
        Selenide.element(By.xpath("//coral-button-label[text()='Done']")).ancestor("button").click();

    }

    private static String getListItemProperty(SelenideElement listItem, String name) {
        for (SelenideElement selenideElement : listItem.$$(".item-property")) {
            if (selenideElement.text().trim().startsWith(name + CoreConstants.SEPARATOR_COLON)) {
                return StringUtils.substringAfter(selenideElement.text(), name + CoreConstants.SEPARATOR_COLON).trim();
            }
        }
        return StringUtils.EMPTY;
    }
}
