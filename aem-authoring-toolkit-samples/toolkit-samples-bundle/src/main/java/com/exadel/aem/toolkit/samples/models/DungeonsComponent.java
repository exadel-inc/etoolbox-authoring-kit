package com.exadel.aem.toolkit.samples.models;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Properties;
import com.exadel.aem.toolkit.api.annotations.widgets.property.Property;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Option;
import com.exadel.aem.toolkit.api.annotations.widgets.select.Select;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Dialog(
        name = "content/dungeons-component",
        title = "Dungeons component",
        description = "Choose a dungeon for your warrior",
        resourceSuperType = "authoring-toolkit/samples/components/content/father-select-component",
        componentGroup = "Toolkit Samples",
        tabs= {
        @Tab(title = FatherSelectComponent.TAB_MAIN),
}
)
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class DungeonsComponent extends FatherSelectComponent{

        private final String DEFAULT_ROTTEN_SWAMPS_TEXT = "rotten swamps, where your nose will suffer from terrible smells,";
        private final String DEFAULT_ICE_VALLEY_TEXT = "ice valley, where you can lose your arm from strong frost,";
        private final String DEFAULT_INTRODUCTION = "Once upon a time in the ";
        private final String DUNGEON_RULES_INTRODUCTION = "There are some rules in this dungeon: ";
        private final String DEFAULT_ENDING = " the warrior has started his adventure.";

        @Extends(value = WarriorDescriptionComponent.class, field = "description")
        @RichTextEditor(
                features = {
                        RteFeatures.SEPARATOR,
                        RteFeatures.SPELLCHECK_CHECKTEXT
                })
        @DialogField(name = "./dungeonRules", label = "Make your own dungeons rules")
        @ValueMapValue
        private String dungeonRules;

        @Select(options = {
                @Option(text = "Rotten swamps", value = "1"),
                @Option(text = "Ice valley", value = "2")
        })
        @DialogField(label = "Dungeons select")
        @Default(values = "1")
        @Properties(value = {@Property(name = "sling:hideChildren", value = "*")})
        @ValueMapValue
        private String dungeonsSelect;

        public String getDungeonRules() {
                return (dungeonRules == null || "".equals(dungeonRules))
                        ? "no rules!"
                        : dungeonRules;
        }

        public String getDungeonDescription() {
                StringBuilder sb = new StringBuilder();

                sb.append(DEFAULT_INTRODUCTION);
                if (dungeonsSelect.equals("1")) {
                        sb.append(DEFAULT_ROTTEN_SWAMPS_TEXT);
                } else {
                        sb.append(DEFAULT_ICE_VALLEY_TEXT);
                }
                sb.append(DEFAULT_ENDING);
                sb.append(DUNGEON_RULES_INTRODUCTION);
                sb.append(getDungeonRules());
                return sb.toString();
        }
}
