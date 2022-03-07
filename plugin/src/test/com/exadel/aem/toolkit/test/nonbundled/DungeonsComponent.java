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
 *//*


package com.exadel.aem.toolkit.test.nonbundled;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.api.annotations.widgets.hyperlink.Hyperlink;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Properties;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.plugin.util.TestConstants;

@Dialog(
    name = TestConstants.DEFAULT_COMPONENT_NAME,
    title = "Dungeons Component",
    description = "Choose a dungeon for your warrior",
    resourceSuperType = "etoolbox-authoring-kit/samples/components/content/parent-select-component",
    componentGroup = "Toolkit Samples"
)
@Accordion(value = @AccordionPanel(title = "Main"))
public class DungeonsComponent extends ParentSelectComponent {

    private static final String LABEL_DUNGEON_RULES = "Make your own dungeons rules";
    private static final String LABEL_DUNGEON_SELECT = "Dungeons select";

    private static final String DEFAULT_ROTTEN_SWAMPS_TEXT = "rotten swamps, where your nose will suffer from terrible smells,";
    private static final String DEFAULT_ICE_VALLEY_TEXT = "ice valley, where you can lose your arm from strong frost,";
    private static final String DEFAULT_RULES = "no rules!";

    @DialogField(label = LABEL_DUNGEON_RULES)
    @Extends(value = WarriorDescriptionComponent.class, field = "description")
    @RichTextEditor(
        features = {
            RteFeatures.SEPARATOR,
            RteFeatures.LISTS_ORDERED,
            RteFeatures.LISTS_UNORDERED
        })
    @Place(in = "Main")
    private String dungeonRules;

    @DependsOn(query = "@dungeon === '1'")
    @Hyperlink(href = "https://media.graphcms.com/BmLxDCt5SaSHWx4rt6xy", text = "Dungeon profile image", target = "_blank")
    private String profileLink;

    @DependsOn(query = "@dungeon === '2'")
    @Hyperlink(href = "https://cg4.cgsociety.org/uploads/images/medium/penemenn-ice-valley-1-11885220-8day.jpg", text = "Dungeon profile image", target = "_blank")
    private String profileLink2;

    @Accordion(
        value = @AccordionPanel(title = LABEL_DUNGEON_SELECT))
    @Place(in = "Main")
    DungeonSelect dungeon;

    static class DungeonSelect {
        @DialogField(label = LABEL_DUNGEON_SELECT)
        @Select(options = {
            @Option(text = "Rotten swamps", value = "1"),
            @Option(text = "Ice valley", value = "2")
        })
        @DependsOnRef(name = "dungeon")
        @Properties(
            value = @Property(name = "sling:hideChildren", value = "*"))
        @Place(in = "Dungeons select")
        String dungeonsSelect;

    }

    public String getDungeonRules() {
        return StringUtils.defaultIfBlank(dungeonRules, DEFAULT_RULES);
    }

    public String getDungeonDescription() {
        if ("1".equals(dungeon.dungeonsSelect)) {
            return DEFAULT_ROTTEN_SWAMPS_TEXT;
        }
        return DEFAULT_ICE_VALLEY_TEXT;
    }
}
*/
