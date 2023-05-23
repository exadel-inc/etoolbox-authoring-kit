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
import org.openqa.selenium.Keys;
import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;

import com.exadel.aem.toolkit.it.base.AemConnection;
import com.exadel.aem.toolkit.it.base.EditModeUtil;
import com.exadel.aem.toolkit.it.base.Render;
import com.exadel.aem.toolkit.it.cases.allowedchildren.ContainerComponent1;
import com.exadel.aem.toolkit.it.cases.allowedchildren.ContainerComponent2;
import com.exadel.aem.toolkit.it.cases.allowedchildren.EditablePage;
import com.exadel.aem.toolkit.it.cases.allowedchildren.ParsysComponent;
import com.exadel.aem.toolkit.it.cases.allowedchildren.StaticPage;

@Render({
    StaticPage.class,
    EditablePage.class,
    ContainerComponent1.class,
    ContainerComponent2.class,
    ParsysComponent.class
})
public class AllowedChildrenTest {

    private static final String BODY = "body";

    private static final String TITLE_AUDIO = "Sample Audio Component";
    private static final String TITLE_CONTAINER_1 = "Container Component 1";
    private static final String TITLE_CONTAINER_2 = "Container Component 2";
    private static final String TITLE_IMAGE = "Sample Image Component";
    private static final String TITLE_TEXT = "Sample Text Component";
    private static final String TITLE_VIDEO = "Sample Video Component";

    @BeforeClass
    public static void login() {
        AemConnection.login();
    }

    @Test
    public void shouldAssignToStaticPageRootContainer() {
        assignToPageRootContainer("static", "parsys");
    }

    @Test
    public void shouldAssignToEditablePageRootContainer() {
        assignToPageRootContainer("editable", "root/grid");
    }

    private void assignToPageRootContainer(String pageType, String container) {
        AemConnection.open("editor.html/content/etoolbox-authoring-kit-test/allowedChildren/" + pageType + "-page/empty.html");

        ElementsCollection componentOptions = EditModeUtil.getInsertChildOptions(
            "/content/etoolbox-authoring-kit-test/allowedChildren/"
                + pageType
                + "-page/empty/jcr:content/"
                + container + "_0/*");
        componentOptions.shouldBe(CollectionCondition.size(2));
        componentOptions.shouldHave(CollectionCondition.itemWithText(TITLE_CONTAINER_1));
        componentOptions.shouldHave(CollectionCondition.itemWithText(TITLE_VIDEO));

        Selenide.$(BODY).sendKeys(Keys.ESCAPE);

        componentOptions = EditModeUtil.getInsertChildOptions(
            "/content/etoolbox-authoring-kit-test/allowedChildren/"
                + pageType
                + "-page/empty/jcr:content/"
                + container
                + "_1/*");
        componentOptions.shouldBe(CollectionCondition.size(3)); // 2 from the "Predefined" group + 1 insertable
        componentOptions.shouldHave(CollectionCondition.itemWithText(TITLE_VIDEO));
    }

    @Test
    public void shouldAssignToComponentInStaticTemplate() {
        assignToComponentInTemplate("static", "parsys");
    }

    @Test
    public void shouldAssignToComponentInEditableTemplate() {
        assignToComponentInTemplate("editable", "root/grid");
    }

    private void assignToComponentInTemplate(String pageType, String container) {
        AemConnection.open("editor.html/content/etoolbox-authoring-kit-test/allowedChildren/" + pageType + "-page/nested-container.html");

        ElementsCollection componentOptions = EditModeUtil.getInsertChildOptions(
            "/content/etoolbox-authoring-kit-test/allowedChildren/"
                + pageType
                + "-page/nested-container/jcr:content/"
                + container
                + "_0/container1/content/*");
        componentOptions.shouldBe(CollectionCondition.size(2));
        componentOptions.shouldHave(CollectionCondition.textsInAnyOrder(TITLE_VIDEO, TITLE_CONTAINER_2));

        Selenide.$(BODY).sendKeys(Keys.ESCAPE);

        componentOptions = EditModeUtil.getInsertChildOptions(
            "/content/etoolbox-authoring-kit-test/allowedChildren/"
                + pageType
                + "-page/nested-container/jcr:content/"
                + container
                + "_1/container2/content/*");
        componentOptions.shouldBe(CollectionCondition.size(4)); // 2 from the "Predefined" group + 2 insertable
        componentOptions.should(CollectionCondition.containExactTextsCaseSensitive(TITLE_VIDEO, TITLE_CONTAINER_1));

        // Inserting an extra container
        componentOptions.filter(Condition.exactText(TITLE_CONTAINER_1)).first().click();
        componentOptions = EditModeUtil.getInsertChildOptions(
            "/content/etoolbox-authoring-kit-test/allowedChildren/"
                + pageType
                + "-page/nested-container/jcr:content/"
                + container
                + "_1/container2/content/container1/content/*");
        componentOptions.shouldBe(CollectionCondition.size(2));
        componentOptions.shouldHave(CollectionCondition.textsInAnyOrder(TITLE_VIDEO, TITLE_CONTAINER_2));
    }

    @Test
    public void shouldAssignToPageUnderPath() {
        AemConnection.open("editor.html/content/etoolbox-authoring-kit-test/allowedChildren/static-page.html");
        ElementsCollection componentOptions = EditModeUtil.getInsertChildOptions(
            "/content/etoolbox-authoring-kit-test/allowedChildren/static-page/jcr:content/parsys_0/*");
        componentOptions.shouldBe(CollectionCondition.size(2));
        componentOptions.shouldHave(CollectionCondition.textsInAnyOrder(TITLE_VIDEO, TITLE_CONTAINER_1));
    }

    @Test
    public void shouldAssignToPageWithTemplate() {
        AemConnection.open("editor.html/content/etoolbox-authoring-kit-test/allowedChildren/static-page.html");
        ElementsCollection componentOptions = EditModeUtil.getInsertChildOptions(
            "/content/etoolbox-authoring-kit-test/allowedChildren/static-page/jcr:content/parsys_1/*");
        componentOptions.shouldBe(CollectionCondition.size(2));
        componentOptions.shouldHave(CollectionCondition.textsInAnyOrder(TITLE_VIDEO, TITLE_CONTAINER_2));
    }

    @Test
    public void shouldAssignToPageWithResourceType() {
        AemConnection.open("editor.html/content/etoolbox-authoring-kit-test/allowedChildren/editable-page.html");

        ElementsCollection componentOptions = EditModeUtil.getInsertChildOptions(
            "/content/etoolbox-authoring-kit-test/allowedChildren/editable-page/jcr:content/root/grid_0/container1/content/*");
        componentOptions.shouldBe(CollectionCondition.size(2));
        componentOptions.shouldHave(CollectionCondition.textsInAnyOrder(TITLE_VIDEO, TITLE_AUDIO));

        Selenide.$(BODY).sendKeys(Keys.ESCAPE);
        componentOptions = EditModeUtil.getInsertChildOptions(
            "/content/etoolbox-authoring-kit-test/allowedChildren/editable-page/jcr:content/root/grid_1/container2/content/container1/content/*");
        componentOptions.shouldBe(CollectionCondition.size(2));
        componentOptions.shouldHave(CollectionCondition.textsInAnyOrder(TITLE_VIDEO, TITLE_AUDIO));
    }

    @Test
    public void shouldAssignToPathWithParents() {
        AemConnection.open("editor.html/content/etoolbox-authoring-kit-test/allowedChildren/editable-page.html");
        ElementsCollection componentOptions = EditModeUtil.getInsertChildOptions(
            "/content/etoolbox-authoring-kit-test/allowedChildren/editable-page/jcr:content/root/grid_0/container1/content/container2/content/*");
        componentOptions.shouldBe(CollectionCondition.size(3));
        componentOptions.shouldHave(CollectionCondition.textsInAnyOrder(TITLE_AUDIO, TITLE_IMAGE, TITLE_TEXT));
        Selenide.$(BODY).sendKeys(Keys.ESCAPE);
    }

    @Test
    public void shouldHandleSelfContainerRules() {
        AemConnection.open("editor.html/content/etoolbox-authoring-kit-test/allowedChildren/static-page.html");
        ElementsCollection componentOptions = EditModeUtil.getInsertChildOptions(
            "/content/etoolbox-authoring-kit-test/allowedChildren/static-page/jcr:content/parsys_0/myparsys");
        componentOptions.shouldBe(CollectionCondition.size(2));
        componentOptions.shouldHave(CollectionCondition.textsInAnyOrder(TITLE_VIDEO, TITLE_AUDIO));
    }
}
