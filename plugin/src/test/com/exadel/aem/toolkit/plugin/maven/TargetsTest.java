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

import java.util.Calendar;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.adapters.DomAdapter;
import com.exadel.aem.toolkit.plugin.targets.Targets;
import com.exadel.aem.toolkit.plugin.utils.XmlFactory;

public class TargetsTest {
    private static final String NN_ROOT = "root";
    private static final String NN_ITEM = "item";
    private static final String NN_SUBITEM = "subitem";
    private static final String NN_SUBSUBITEM = "subsubitem";
    private static final String PN_INT_ARRAY = "intArray";
    private static final String PN_BOOL_ARRAY = "boolArray";
    private static final String PN_DATE_ARRAY = "dateArray";
    private static final String PN_ORDINAL = "ordinal";

    private static final int TIER_1_CHILD_COUNT = 10;

    private Target testable;

    @Before
    public void initTarget() {
        testable = Targets.newInstance(NN_ROOT, Scopes.COMPONENT);
        for (int i = 0; i < TIER_1_CHILD_COUNT; i++) {
            Target item = testable.createTarget(NN_ITEM + i).attribute(PN_ORDINAL, i);
            if (i % 2 == 0) {
                item.getOrCreateTarget(NN_SUBITEM + i)
                    .attribute(PN_ORDINAL, i-1)
                    .attribute(PN_ORDINAL, i);
                if (i == 0) {
                    item
                        .attribute(PN_INT_ARRAY, new long[] {1, 2, 3})
                        .attribute(PN_BOOL_ARRAY, new boolean[] {true, true, false, true})
                        .attribute(PN_DATE_ARRAY, new Date[] {getDate(2020,10,01), getDate(2021, 1, 10)})
                        .createTarget(NN_SUBITEM + i + CoreConstants.SEPARATOR_SLASH + NN_SUBSUBITEM + i)
                            .attribute(PN_ORDINAL, i);
                }
            }
        }
   }

    @Test
    public void testGenericProperties() {
        Assert.assertEquals("root", testable.getName());
        Assert.assertEquals(Scopes.COMPONENT, testable.getScope());
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
    public void testNodeInjection() {
        testable.getTarget("item0").addTarget(testable.getTarget("item1"));
        Assert.assertNotNull(testable.getTarget("item0/item1"));
        Assert.assertEquals(TIER_1_CHILD_COUNT - 1, testable.getChildren().size());

        testable.getTarget("item0").addTarget(testable.getTarget("item2"), testable.getTarget("item0/item1"));
        testable.getTarget("item0").addTarget(testable.getTarget("item3"), testable.getTarget("item0/item1"), true);
        Assert.assertEquals(TIER_1_CHILD_COUNT - 3, testable.getChildren().size());
        Assert.assertEquals("item2", testable.getTarget("item0").getChildren().get(1).getName());
        Assert.assertEquals("item3", testable.getTarget("item0").getChildren().get(3).getName());

        testable.addTarget(testable.getTarget("item0"));
        Assert.assertEquals(TIER_1_CHILD_COUNT - 3, testable.getChildren().size());
        Assert.assertEquals("item0", testable.getChildren().get(TIER_1_CHILD_COUNT - 4).getName());
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
        Assert.assertEquals(
            "{Long}[1,2,3]",
            ((Element) document.getDocumentElement().getFirstChild()).getAttribute(PN_INT_ARRAY));
        Assert.assertEquals(
            "{Boolean}[true,true,false,true]",
            ((Element) document.getDocumentElement().getFirstChild()).getAttribute(PN_BOOL_ARRAY));
        Assert.assertEquals(
            "{Date}[2020-11-01T00:00:00.000+00:00,2021-02-10T00:00:00.000+00:00]",
            ((Element) document.getDocumentElement().getFirstChild()).getAttribute(PN_DATE_ARRAY).replaceAll("\\+\\d{2}:", "+00:"));
    }

    private static Date getDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return Date.from(calendar.toInstant());
    }
}
