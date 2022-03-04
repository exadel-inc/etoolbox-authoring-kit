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
package com.exadel.aem.toolkit.test.nonbundled;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnActions;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRef;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnRefTypes;
import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOnTab;
import com.exadel.aem.toolkit.api.annotations.container.PlaceOnTab;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.main.Dialog;
import com.exadel.aem.toolkit.api.annotations.widgets.Checkbox;
import com.exadel.aem.toolkit.api.annotations.widgets.DialogField;
import com.exadel.aem.toolkit.api.annotations.widgets.Hidden;
import com.exadel.aem.toolkit.api.annotations.widgets.autocomplete.Autocomplete;
import com.exadel.aem.toolkit.api.annotations.widgets.autocomplete.AutocompleteDatasource;
import com.exadel.aem.toolkit.api.annotations.widgets.common.TypeHint;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DatePicker;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DatePickerType;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DateTimeValue;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioButton;
import com.exadel.aem.toolkit.api.annotations.widgets.radio.RadioGroup;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.AllowElement;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.Characters;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.HtmlLinkRules;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.HtmlPasteRules;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.IconMapping;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.LinkTarget;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.ParagraphFormat;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RichTextEditor;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.RteFeatures;
import com.exadel.aem.toolkit.api.annotations.widgets.rte.Style;

@Dialog(
        name = "content/warrior-description-component",
        title = "Warrior Description Component",
        description = "Tell us about your warrior",
        resourceSuperType = "PathConstants.FOUNDATION_PARBASE_PATH",
        componentGroup = "GroupConstants.COMPONENT_GROUP",
        tabs = {
                @Tab(title = WarriorDescriptionComponent.TAB_MAIN),
                @Tab(title = WarriorDescriptionComponent.TAB_TASTES),
                @Tab(title = WarriorDescriptionComponent.TAB_FRUITS),
                @Tab(title = WarriorDescriptionComponent.TAB_MOVIES)
        },
        extraClientlibs = "etoolbox-authoring-kit.samples.authoring"
)
@DependsOnTab(tabTitle = WarriorDescriptionComponent.TAB_FRUITS, query = "@likesFruit")
@DependsOnTab(tabTitle = WarriorDescriptionComponent.TAB_MOVIES, query = "@likesMovies")
public class WarriorDescriptionComponent {

    static final String TAB_MAIN = "Main info";
    static final String TAB_TASTES = "Tastes";
    static final String TAB_FRUITS = "Fruits";
    static final String TAB_MOVIES = "Movies";

    private static final String LABEL_BIRTHDAY = "Birthday";
    private static final String DESCRIPTION_BIRTHDAY = "Enter the birthday of your warrior";

    private static final String LABEL_CHARACTER = "Character";
    private static final String DESCRIPTION_CHARACTER = "Choose character of your warrior";

    private static final String LABEL_LIKES_FRUIT = "Does your warrior like fruit?";
    private static final String LABEL_FRUIT = "Favorite fruit";
    private static final String LABEL_LIKES_MOVIES = "Does your warrior like movies?";
    private static final String LABEL_MOVIES = "Favorite movies";


    private static final String DESCRIPTION_TEMPLATE = "%s was born on a cold but bright day %s. He would %s. %s, and %s";
    private static final String DEFAULT_BIRTHDAY = "29.03.2000";
    private static final String DEFAULT_CHARACTER = "always smile creepily";
    private static final String DEFAULT_FRUIT_TEXT = "He doesn't like fruit";
    private static final String DEFAULT_MOVIES_TEXT = "He doesn't like movies";

    private static final String FRUIT_TEXT = "His favorite fruit: ";
    private static final String MOVIES_TEXT = "his favorite movie genres: ";

    @DialogField
    @PlaceOnTab(WarriorDescriptionComponent.TAB_MAIN)
    @RichTextEditor(
            features = {
                    RteFeatures.UNDO_UNDO,
                    RteFeatures.UNDO_REDO,
                    RteFeatures.SEPARATOR,
                    RteFeatures.Popovers.EDIT_ALL,
                    RteFeatures.SEPARATOR,
                    RteFeatures.Popovers.FORMAT_ALL,
                    RteFeatures.Popovers.STYLES,
                    RteFeatures.Popovers.JUSTIFY_ALL,
                    RteFeatures.Popovers.LISTS_ALL,
                    RteFeatures.SEPARATOR,
                    RteFeatures.MISCTOOLS_SPECIALCHARS,
                    RteFeatures.SEPARATOR,
                    RteFeatures.Popovers.PARAFORMAT
            },
            htmlPasteRules = @HtmlPasteRules(
                    allowImages = false,
                    allowLists = AllowElement.DISALLOW,
                    allowTables = AllowElement.DISALLOW
            ),
            externalStyleSheets = {"/apps/etoolbox-authoring-kit/samples/components/content/warrior-description-component/clientlib/rte.styles/rte-style.css"},
            styles = {
                    @Style(cssName = "rte-style", text = "Aggressive style"),
            },
            icons = {
                    @IconMapping(command = "#styles", icon = "textColor"),
                    @IconMapping(command = "#edit", icon = "textEdit")
            },
            htmlLinkRules = @HtmlLinkRules(
                    targetInternal = LinkTarget.MANUAL,
                    targetExternal = LinkTarget.BLANK,
                    protocols = {"http://", "https://"},
                    defaultProtocol = "https://"
            ),
            specialCharacters = {
                    @Characters(name = "Copyright", entity = "&copy"),
                    @Characters(name = "Euro sign", entity = "&#x20AC"),
                    @Characters(name = "Trademark", entity = "&#x2122"),
                    @Characters(rangeStart = 48, rangeEnd = 70),
            },
            formats = {
                    @ParagraphFormat(tag = "h1", description = "My custom header"),
                    @ParagraphFormat(tag = "h2", description = "My custom subheader")
            }
    )
    private String description;

    @DialogField(
            label = LABEL_BIRTHDAY,
            description = DESCRIPTION_BIRTHDAY
    )
    @DatePicker(
            type = DatePickerType.DATE,
            displayedFormat = "DD.MM.YYYY",
            valueFormat = "DD.MM.YYYY",
            maxDate = @DateTimeValue(day = 1, month = 12, year = 2019),
            typeHint = TypeHint.STRING
    )
    @PlaceOnTab(WarriorDescriptionComponent.TAB_MAIN)
    private String birthday;

    @DialogField(label = LABEL_LIKES_FRUIT)
    @Checkbox
    @DependsOnRef(name = "likesFruit")
    @PlaceOnTab(WarriorDescriptionComponent.TAB_TASTES)
    private boolean likesFruit;

    @DialogField(label = LABEL_FRUIT)
    @Autocomplete(
            multiple = true,
            forceSelection = true,
            datasource = @AutocompleteDatasource(namespaces = "fruit")
    )
    @PlaceOnTab(WarriorDescriptionComponent.TAB_FRUITS)
    private String[] fruit;

    @DialogField(
            label = LABEL_CHARACTER,
            description = DESCRIPTION_CHARACTER,
            ranking = 4
    )
    @RadioGroup(buttons = {
            @RadioButton(text = "Funny", value = "always smile creepily", checked = true),
            @RadioButton(text = "Sad", value = "always steal your handkerchief"),
            @RadioButton(text = "Angry", value = "always ignore you")
    })
    @PlaceOnTab(WarriorDescriptionComponent.TAB_MAIN)
    private String character;

    @DialogField(label = LABEL_LIKES_MOVIES)
    @Checkbox
    @DependsOnRef(name = "likesMovies")
    @PlaceOnTab(WarriorDescriptionComponent.TAB_TASTES)
    private boolean likesMovies;

    @DialogField
    @Hidden
    @DependsOn(query = "'../../colorTheme'", action = DependsOnActions.FETCH)
    @DependsOnRef(name = "isDarkColorTheme", type = DependsOnRefTypes.BOOLSTRING)
    private boolean isDarkColorTheme;

    @DialogField(label = LABEL_MOVIES)
    @Autocomplete(
            multiple = true,
            forceSelection = true,
            datasource = @AutocompleteDatasource(namespaces = "movies")
    )
    @DependsOn(query = "@isDarkColorTheme", action = "namespaceFilter")
    @PlaceOnTab(WarriorDescriptionComponent.TAB_MOVIES)
    private String[] movies;

}
