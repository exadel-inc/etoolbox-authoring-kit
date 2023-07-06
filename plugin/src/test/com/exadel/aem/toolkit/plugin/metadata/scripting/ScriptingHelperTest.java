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
package com.exadel.aem.toolkit.plugin.metadata.scripting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.exadel.aem.toolkit.api.annotations.main.Setting;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.TextField;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ScriptedFieldset1;
import com.exadel.aem.toolkit.plugin.handlers.common.cases.components.ScriptedFieldset2;
import com.exadel.aem.toolkit.plugin.sources.Sources;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

public class ScriptingHelperTest {

    private static final String SCRIPT_CONTAINER = "Lorem {ipsum dolor} ${sit amet}, consectetur \"${adipiscing} elit\","
        + "sed do @{eiusmod tempor} incididunt ut @labore et @dolore 'magna @aliqua'";

    @Test
    public void testInlineScriptExtraction() {
        SubstringMatcher substringMatcher = new SubstringMatcher(
            SCRIPT_CONTAINER,
            DialogConstants.OPENING_CURLY,
            DialogConstants.CLOSING_CURLY,
            Arrays.asList(CoreConstants.SEPARATOR_AT, "$"));
        SubstringMatcher.Substring next = substringMatcher.next();
        List<String> substrings = new ArrayList<>();
        while (next != null) {
            substrings.add(next.getContent());
            next = substringMatcher.next();
        }
        Assert.assertArrayEquals(
            new String[] {"${sit amet}", "@{eiusmod tempor}"},
            substrings.toArray(new String[0]));
    }

    @Test
    public void testMarkerWordExtraction() {
        SubstringMatcher substringMatcher = new SubstringMatcher(
            SCRIPT_CONTAINER,
            CoreConstants.SEPARATOR_AT);
        SubstringMatcher.Substring next = substringMatcher.next();
        List<String> substrings = new ArrayList<>();
        while (next != null) {
            substrings.add(next.getContent());
            next = substringMatcher.next();
        }
        Assert.assertArrayEquals(
            new String[] {"@labore", "@dolore"},
            substrings.toArray(new String[0]));
    }

    @Test
    public void testInterpolateMetadata() throws NoSuchMethodException {
        Source source = Sources.fromClass(ScriptedFieldset1.class);
        Assert.assertNotNull(source);

        Setting[] classLevelSettings = source.adaptTo(Setting[].class);
        Assert.assertNotNull(classLevelSettings);
        Assert.assertEquals("scripted{@value}", classLevelSettings[0].value());

        source = Sources.fromMember(ScriptedFieldset1.class.getDeclaredMethod("getHeading"));
        Property[] properties = source.adaptTo(Property[].class);
        Assert.assertNotNull(properties);
        Assert.assertEquals(2, properties.length);
        Assert.assertEquals("/subnode_1", properties[0].name());
        Assert.assertEquals("/subnode_2", properties[1].name());
    }

    @Test
    public void testInterpolateReflectiveData1() throws NoSuchFieldException {
        Source source = Sources.fromMember(ScriptedFieldset2.class.getDeclaredField("text"));
        Assert.assertNotNull(source);
        DialogField dialogField = source.adaptTo(DialogField.class);
        Assert.assertEquals("Field text", dialogField.label());
        Assert.assertEquals("In class ScriptedFieldset2", dialogField.description());
        TextField textField = source.adaptTo(TextField.class);
        Assert.assertEquals("Imported Field text", textField.value());
        Assert.assertEquals("Hello World", textField.emptyText());
    }

    @Test
    public void testInterpolateReflectiveData2() throws NoSuchFieldException {
        Source source = Sources.fromMember(ScriptedFieldset2.class.getDeclaredField("extensionText"));
        Assert.assertNotNull(source);
        DialogField dialogField = source.adaptTo(DialogField.class);
        Assert.assertEquals("Extension text", dialogField.label());
        Assert.assertEquals("Has parent interface", dialogField.description());
    }
}
