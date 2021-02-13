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

package com.exadel.aem.toolkit.plugin.maven;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.adapters.DomAdapter;
import com.exadel.aem.toolkit.plugin.target.Targets;
import com.exadel.aem.toolkit.plugin.util.XmlFactory;

public class TargetsTest {
    private static final String NN_ROOT = "root";
    private static final String NN_ITEM = "item";
    private static final String NN_SUBITEM = "subitem";
    private static final String NN_SUBSUBITEM = "subsubitem";
    private static final String PN_ORDINAL = "ordinal";

    private static final int TIER_1_CHILD_COUNT = 10;

    private Target testable;

    @Before
    public void initTarget() {
        testable = Targets.newInstance(NN_ROOT, XmlScope.COMPONENT);
        for (int i = 0; i < TIER_1_CHILD_COUNT; i++) {
            Target item = testable.createTarget(NN_ITEM + i).attribute(PN_ORDINAL, i);
            if (i % 2 == 0) {
                item.getOrCreateTarget(NN_SUBITEM + i)
                    .attribute(PN_ORDINAL, i-1)
                    .attribute(PN_ORDINAL, i)
                    .namePostfix("_checked");
                if (i == 0) {
                    item.createTarget(NN_SUBITEM + i + "/" + NN_SUBSUBITEM + i).attribute(PN_ORDINAL, i);
                }
            }
        }
   }

    @Test
    public void testGenericProperties() {
        Assert.assertEquals("root", testable.getName());
        Assert.assertEquals(XmlScope.COMPONENT, testable.getScope());
        Assert.assertEquals(TIER_1_CHILD_COUNT, testable.getChildren().size());
        Assert.assertEquals(testable, testable.getChildren().get(0).getParent());
        Assert.assertEquals(testable, testable.getChildren().get(0).getChildren().get(0).getParent().getParent());
        Assert.assertEquals("{Long}1", testable.getChildren().get(1).getAttribute(PN_ORDINAL));
    }

    @Test
    public void testNodeManipulation() {
        Assert.assertTrue(testable.exists("item0"));
        Assert.assertFalse(testable.exists("item" + TIER_1_CHILD_COUNT));

        Assert.assertNotNull(testable.getTarget("item0/subitem0"));
        Assert.assertNotNull(testable.getTarget("../../item0/subitem0")); // '..' path returns the current node if no parent
        Assert.assertEquals("subitem0", testable.getTarget("item2/subitem2/../../item0/subitem0").getName());
        Assert.assertNull(testable.getTarget("item2/subitem3/../../item0/subitem0"));

        Assert.assertNotNull(
            testable.getOrCreateTarget("item2/\"sub/item3\"/../../item0/subitem0") // the slash between "sub/item3" is escaped
        );                                                                         // and does not produce a separate node
        Assert.assertEquals("subitem3", testable.getTarget("item2").getChildren().get(1).getName());
        Assert.assertEquals(
            "{Long}10",
            testable.createTarget("item10/subitem10").attribute(PN_ORDINAL, 10).getAttribute(PN_ORDINAL, "{Long}9"));

        testable.removeTarget(".."); // Will have no effect
        testable.removeTarget("fake"); // Will have no effect
        testable.removeTarget("item2/subitem3");
        Assert.assertNull(testable.getTarget("item2/subitem3"));
    }

    @Test
    public void testNodeTraversing() {
        Assert.assertEquals(TIER_1_CHILD_COUNT / 2, testable.findChildren(t -> t.getName().startsWith(NN_SUBITEM)).size());
        Assert.assertEquals(3, testable.findChildren(t -> t.getAttribute(PN_ORDINAL).equals("{Long}0")).size());
        Target subsubitem = testable.findChild(t -> !t.getTarget("../../..").equals(t.getTarget("../..")));
        Assert.assertEquals(NN_SUBSUBITEM + 0, subsubitem.getName());
        Assert.assertEquals(testable, subsubitem.findParent(t -> t.getName().equals(NN_ROOT)));
    }

    @Test
    public void testXmlExport() throws ParserConfigurationException {
        Document document = testable.adaptTo(DomAdapter.class).composeDocument(XmlFactory.newDocument());
        Assert.assertEquals(NN_ROOT, document.getDocumentElement().getTagName());
    }
}
