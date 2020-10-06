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

package com.exadel.aem.toolkit.samples.models;

import com.exadel.aem.toolkit.api.annotations.container.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOn;
import com.exadel.aem.toolkit.api.annotations.container.Accordion;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnAccordion;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.AccordionWidget;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Properties;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.samples.constants.GroupConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Dialog(
        name = "content/dungeons-component",
        title = "Dungeons Component",
        description = "Choose a dungeon for your warrior",
        resourceSuperType = "authoring-toolkit/samples/components/content/parent-select-component",
        componentGroup = GroupConstants.COMPONENT_GROUP,
        accordionTabs = {
                @AccordionPanel(title = "Main")
        }
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
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
    @ValueMapValue
    @PlaceOn("Main")
    private String dungeonRules;

    @AccordionWidget(title = "Dungeons select", panels = {@AccordionPanel(title = "Dungeons select")})
    @PlaceOn("Main")
    DungeonSelect dungeonSelect;

    static class DungeonSelect {
        @Select(options = {
                @Option(text = "Rotten swamps", value = "1"),
                @Option(text = "Ice valley", value = "2")
        })
        @DialogField(label = "Dungeons select")
        @Default(values = "1")
        @Properties(value = {@Property(name = "sling:hideChildren", value = "*")})
        @ValueMapValue
        @PlaceOn("Dungeons select")
        String dungeonsSelect;

    }


    public String getDungeonRules() {
        return StringUtils.defaultIfBlank(dungeonRules, DEFAULT_RULES);
    }

    public String getDungeonDescription() {
        if ("1".equals(dungeon)) {
            return DEFAULT_ROTTEN_SWAMPS_TEXT;
        }
        return DEFAULT_ICE_VALLEY_TEXT;
    }
}
