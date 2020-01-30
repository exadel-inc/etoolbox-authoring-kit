package com.exadel.aem.toolkit.samples.models;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;

import com.exadel.aem.toolkit.api.annotations.assets.dependson.DependsOn;
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
import com.exadel.aem.toolkit.api.annotations.widgets.rte.*;
import com.exadel.aem.toolkit.samples.constants.GroupConstants;
import com.exadel.aem.toolkit.samples.constants.PathConstants;

@Dialog(
        name = "content/warrior-description-component",
        title = "Warrior Description Component",
        description = "Tell us about your warrior",
        resourceSuperType = PathConstants.FOUNDATION_PARBASE_PATH,
        componentGroup = GroupConstants.COMPONENT_GROUP,
        tabs = {
                @Tab(title = WarriorDescriptionComponent.TAB_MAIN),
                @Tab(title = WarriorDescriptionComponent.TAB_TASTES),
                @Tab(title = WarriorDescriptionComponent.TAB_FRUIT),
                @Tab(title = WarriorDescriptionComponent.TAB_FILMS)
        }
)
@DependsOnTab(tabTitle = WarriorDescriptionComponent.TAB_FRUIT, query = "@isLikesFruit")
@DependsOnTab(tabTitle = WarriorDescriptionComponent.TAB_FILMS, query = "@isLikesFilms")
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class WarriorDescriptionComponent {

    static final String TAB_MAIN = "Main info";
    static final String TAB_TASTES = "Tastes";
    static final String TAB_FRUIT = "Fruit";
    static final String TAB_FILMS = "Films";

    private static final String DESCRIPTION_TEMPLATE = "%s was born on the cold but bright day %s. He would %s. %s, and %s";
    private static final String DEFAULT_BIRTHDAY = "29.03.2000";
    private static final String DEFAULT_CHARACTER = "always creepy smiles";
    private static final String DEFAULT_FRUIT_TEXT = "He doesn't like fruit";
    private static final String DEFAULT_FILMS_TEXT = "he doesn't like films";

    private static final String FRUIT_TEXT = "His favorite fruit: ";
    private static final String FILMS_TEXT = "his favorite film genres: ";
    private static final String TAGS_DELIMITER = ", ";

    @Inject
    private ResourceResolver resourceResolver;

    @Self
    private Resource resource;

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
            externalStyleSheets = {PathConstants.EXTERNAL_STYLE_PATH},
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
    @DialogField
    @ValueMapValue
    private String description;

    @PlaceOnTab(WarriorDescriptionComponent.TAB_MAIN)
    @DatePicker(
            type = DatePickerType.DATE,
            displayedFormat = "DD.MM.YYYY",
            valueFormat = "DD.MM.YYYY",
            maxDate = @DateTimeValue(day = 1, month = 12, year = 2019),
            typeHint = TypeHint.STRING
    )
    @DialogField(
            label = "Birthday",
            description = "Enter birthday of you warrior"
    )
    @ValueMapValue
    private String birthday;

    @DependsOnRef(name = "isLikesFruit")
    @PlaceOnTab(WarriorDescriptionComponent.TAB_TASTES)
    @DialogField(label = "Does your warrior like fruit?")
    @Checkbox
    @ValueMapValue
    private boolean isLikesFruit;

    @PlaceOnTab(WarriorDescriptionComponent.TAB_FRUIT)
    @Autocomplete(
            multiple = true,
            forceSelection = true,
            datasource = @AutocompleteDatasource(namespaces = {"fruit"})
    )
    @DialogField(label = "Favorite fruit")
    @ValueMapValue
    private String[] fruit;

    @PlaceOnTab(WarriorDescriptionComponent.TAB_MAIN)
    @RadioGroup(buttons = {
            @RadioButton(text = "Funny", value = "always creepy smiles", checked = true),
            @RadioButton(text = "Sad", value = "always steals your handkerchief"),
            @RadioButton(text = "Angry", value = "always ignores you")
    })
    @DialogField(
            label = "Character",
            description = "Choose character of your warrior",
            ranking = 4
    )
    @ValueMapValue
    private String character;

    @DependsOnRef(name = "isLikesFilms")
    @PlaceOnTab(WarriorDescriptionComponent.TAB_TASTES)
    @Checkbox
    @DialogField(label = "Does your warrior like films?")
    @ValueMapValue
    private boolean isLikesFilms;

    @Hidden
    @DependsOn(query = "@parentPath", action = "getParentColorTheme")
    @DependsOnRef(name = "isDarkColorTheme", type = DependsOnRefTypes.BOOLSTRING)
    private boolean isDarkColorTheme;

    @PlaceOnTab(WarriorDescriptionComponent.TAB_FILMS)
    @DependsOn(query = "@isDarkColorTheme", action = "namespaceFilter")
    @Autocomplete(
            multiple = true,
            forceSelection = true,
            datasource = @AutocompleteDatasource(namespaces = {"films"})
    )
    @DialogField(label = "Favorite films")
    @ValueMapValue
    private String[] films;

    @DependsOnRef(name = "parentPath")
    @Hidden
    private String parentPath;

    @PostConstruct
    public void init() throws PersistenceException {
        ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
        if (valueMap == null) {
            return;
        }
        String parentPath = Optional.ofNullable(resource.getParent())
                .map(Resource::getParent)
                .map(Resource::getPath)
                .orElse("");
        valueMap.put("parentPath", parentPath);
        resource.getResourceResolver().commit();
    }

    public String getWarriorName() {
        return Optional.ofNullable(resource.getParent())
                .map(Resource::getParent)
                .map(Resource::getValueMap)
                .map((valueMap) -> valueMap.get("warriorName", String.class))
                .orElse(WarriorComponent.DEFAULT_NAME);
    }

    public String getDescription() {
        return description;
    }

    public String getBirthday() {
        return StringUtils.defaultIfBlank(birthday, DEFAULT_BIRTHDAY);
    }

    public String getFilms() {
        if (isLikesFilms && films != null) {
            String filmsString = Arrays.stream(films)
                    .map(film -> film.replaceAll("^.*/(.*)$", "$1"))
                    .collect(Collectors.joining(TAGS_DELIMITER));
            return FILMS_TEXT + filmsString;
        }
        return DEFAULT_FILMS_TEXT;
    }

    public String getFruit() {
        if (isLikesFruit && fruit != null) {
            TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
            if (tagManager != null) {
                return (FRUIT_TEXT +
                        Arrays.stream(fruit)
                                .map(tagManager::resolve)
                                .filter(Objects::nonNull)
                                .map(Tag::getTitle)
                                .collect(Collectors.joining(TAGS_DELIMITER)));
            }
        }
        return DEFAULT_FRUIT_TEXT;
    }

    public String getInitDescription() {
        return String.format(DESCRIPTION_TEMPLATE, getWarriorName(), getBirthday(), getCharacter(), getFruit(), getFilms());
    }

    public String getCharacter() {
        return StringUtils.defaultIfBlank(character, DEFAULT_CHARACTER);
    }
}
