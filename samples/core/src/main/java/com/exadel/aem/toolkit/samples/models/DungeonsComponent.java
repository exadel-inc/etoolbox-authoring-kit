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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.layouts.Accordion;
import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.layouts.Place;
import com.exadel.aem.toolkit.api.annotations.main.AemComponent;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.api.annotations.widgets.Hyperlink;
import com.exadel.aem.toolkit.api.annotations.widgets.Include;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Properties;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import com.exadel.aem.toolkit.samples.constants.GroupConstants;

@AemComponent(
    path = "content/dungeons-component",
    title = "Dungeons Component",
    description = "Choose a dungeon for your warrior",
    resourceSuperType = "etoolbox-authoring-kit/samples/components/content/parent-select-component",
    componentGroup = GroupConstants.COMPONENT_GROUP
)
@Dialog
@Accordion(value = @AccordionPanel(title = "Main"))
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class DungeonsComponent {

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
    @Place("Main")
    @ValueMapValue
    private String dungeonRules;

    @Accordion(
        value = @AccordionPanel(title = LABEL_DUNGEON_SELECT))
    @Place("Main")
    @Self
    DungeonSelect dungeon;

    public String getDungeonRules() {
        return StringUtils.defaultIfBlank(dungeonRules, DEFAULT_RULES);
    }

    public String getDungeonDescription() {
        if ("1".equals(dungeon.dungeonsSelect)) {
            return DEFAULT_ROTTEN_SWAMPS_TEXT;
        }
        return DEFAULT_ICE_VALLEY_TEXT;
    }

    @Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
    public static class DungeonSelect {

        @Default(values = "1")
        @DialogField(label = LABEL_DUNGEON_SELECT)
        @Select(options = {
            @Option(text = "Rotten swamps", value = "1"),
            @Option(text = "Ice valley", value = "2")
        })
        @DependsOnRef(name = "dungeon")
        @Properties(@Property(name = "sling:hideChildren", value = "*"))
        @Place("Dungeons select")
        @ValueMapValue
        String dungeonsSelect;

        @Hyperlink(
            href = "https://i.picsum.photos/id/127/4032/2272.jpg?hmac=QFoFT2_eb_DCqjdlj09UFgUHwI_zefDTBdECRz9lO5Q",
            text = "Dungeon profile image",
            target = "_blank"
        )
        @DependsOn(query = "@dungeon === '1'")
        private String profileLink;

        @Hyperlink(
            href = "https://i.picsum.photos/id/13/2500/1667.jpg?hmac=SoX9UoHhN8HyklRA4A3vcCWJMVtiBXUg0W4ljWTor7s",
            text = "Dungeon profile image",
            target = "_blank")
        @DependsOn(query = "@dungeon === '2'")
        private String profileLink2;

        @Include(
            path = "/content/etoolbox-authoring-kit/samples/resources/jcr:content/external"
        )
        private String externalResourceHolder;
    }
}
