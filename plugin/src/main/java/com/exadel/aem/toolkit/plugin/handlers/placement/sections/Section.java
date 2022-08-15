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
package com.exadel.aem.toolkit.plugin.handlers.placement.sections;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.layouts.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.layouts.Column;
import com.exadel.aem.toolkit.api.annotations.layouts.Tab;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.adapters.PlaceSetting;

/**
 * Represents an abstraction of a container section such as {@code Tab} or {@code AccordionPanel} that contains a list
 * of members designed to be rendered within this container element. Used to compose an ordered "containers to container
 * members" build-up for a component class
 */
public abstract class Section {

    private final List<Source> sources;
    private final boolean isLayout;

    private String titlePrefix;

    /**
     * Creates a new {@code SectionHelper} with an empty list of associated members
     * @param isLayout True if the current section is a dialog layout section; false if it is a dialog widget section
     */
    Section(boolean isLayout) {
        this.isLayout = isLayout;
        this.sources = new ArrayList<>();
    }

    /* ---------------
       Title accessors
       --------------- */

    /**
     * Retrieves the {@code title} value of the annotation this section is bound to
     * @return String value
     */
    public abstract String getTitle();

    /**
     * Assigns to the current instance the title prefix. Normally the prefix represents the "hierarchy" of parent
     * container sections given by their titles, slash-separated
     * @param value String value, nullable
     */
    public void setTitlePrefix(String value) {
        titlePrefix = value;
    }

    /**
     * Retrieves the complete title of this {@code Section} that consists of {@code jcr:title} values of parent sections
     * in the markup plus the {@code title} value of the annotation this section is created from. This is normally used
     * for attributing dialog members with respective sections
     * @return String value
     */
    private String getFullTitle() {
        return StringUtils.stripEnd(StringUtils.defaultString(titlePrefix), CoreConstants.SEPARATOR_SLASH)
            + CoreConstants.SEPARATOR_SLASH
            + getTitle();
    }

    /* ---------------
       Other accessors
       --------------- */

    /**
     * Returns true if the current section is a dialog layout section; false if it is a dialog widget section
     * @return True or false
     */
    boolean isLayout() {
        return isLayout;
    }

    /**
     * Returns true if the current section is a dedicated section that represents an ignored block in the current
     * layout
     * @return True or false
     */
    public boolean isIgnored() {
        return false;
    }

    /**
     * Gets the collection of {@code Source}s associated with the current container
     * @return {@code List<Source>} instance, non-null
     */
    public List<Source> getSources() {
        return sources;
    }

    /* ------------------------
       Section-to-section logic
       ------------------------ */

    /**
     * Merges a foreign {@code ContainerInfo} to the current instance, basically by adding other instance's fields while
     * preserving the same  reference
     * @param other Foreign {@code ContainerInfo} object
     */
    public void merge(Section other) {
        sources.addAll(other.getSources());
    }

    /**
     * Gets whether the current section corresponds to the given title with either a short or a full variant
     * @param title String value
     * @return True or false
     */
    public boolean isMatch(String title) {
        return StringUtils.equalsAny(title, getTitle(), getFullTitle());
    }

    /* -----------------------
       Section-to-member logic
       ----------------------- */

    /**
     * Gets whether the provided member source object fits into this section judging by its placement setting
     * @param member {@code Source} object representing a non-null member source
     * @return True or false
     */
    public boolean canContain(Source member) {
        return canContain(member, false);
    }

    /**
     * Gets whether the provided member source object fits into this section judging by its placement setting
     * @param member         {@code Source} object representing a non-null member source
     * @param allowUndefined True to accept members that do not have a placement setting (can be of use if this is the
     *                       first or "default" section in a section array). Note: this flag is only applicable to
     *                       layout sections. In-dialog sections do not allow members with the undefined placement
     * @return True or false
     */
    public boolean canContain(Source member, boolean allowUndefined) {
        if (!(member instanceof MemberSource)) {
            return false;
        }
        if (!member.tryAdaptTo(PlaceSetting.class).isPresent()) {
            return isLayout && allowUndefined;
        }

        String placeValue = StringUtils.strip(member.adaptTo(PlaceSetting.class).getValue(), CoreConstants.SEPARATOR_SLASH);
        return StringUtils.equals(placeValue, getTitle())
            || StringUtils.equals(placeValue, getFullTitle())
            || (StringUtils.isBlank(placeValue) && isLayout && allowUndefined);
    }

    /* ---------------
       Markup creation
       --------------- */

    /**
     * Produces for further rendering the data structure that represents a container section with required attributes
     * set
     * @param host {@code Target} object represents the container to append section nodes to
     * @return {@code Target} element being a child of the given target, or the provided target itself
     */
    public abstract Target createItemsContainer(Target host);


    /* ---------------
       Factory methods
       --------------- */

    /**
     * Creates a new {@link Section} instance for the given {@code Annotation} taking into account its type
     * @param value    The {@code Annotation} object to process
     * @param isLayout True if the current section is a dialog layout section; false if it is a dialog widget section
     * @return {@code Section} instance, or null in case of a null or unsupported annotation passed as the argument
     */
    @SuppressWarnings("deprecation") // Processing of container.Tab is retained for compatibility and will be removed
    // in a version after 2.0.2
    public static Section from(Annotation value, boolean isLayout) {
        if (value == null) {
            return null;
        }
        if (value.annotationType().equals(Tab.class)) {
            return from((Tab) value, isLayout);
        }
        if (value.annotationType().equals(com.exadel.aem.toolkit.api.annotations.container.Tab.class)) {
            return from((com.exadel.aem.toolkit.api.annotations.container.Tab) value, isLayout);
        }
        if (value.annotationType().equals(AccordionPanel.class)) {
            return from((AccordionPanel) value, isLayout);
        }
        if (value.annotationType().equals(Column.class)) {
            return from((Column) value, isLayout);
        }
        return null;
    }

    /**
     * Creates a new {@link Section} instance for the given {@link Tab} instance
     * @param value    The {@code Tab} object to process
     * @param isLayout True if the current section is a dialog layout section; false if it is a dialog widget section
     * @return {@code Section} instance, or null in case of a null object passed as the argument
     */
    public static Section from(Tab value, boolean isLayout) {
        if (value == null) {
            return null;
        }
        return new TabSection(value, isLayout);
    }

    /**
     * Creates a new {@link Section} instance for the given {@link com.exadel.aem.toolkit.api.annotations.container.Tab}
     * instance
     * @param value    The {@code Tab} object to process
     * @param isLayout True if the current section is a dialog layout section; false if it is a dialog widget section
     * @return {@code Section} instance, or null in case of a null object passed as the argument
     */
    @SuppressWarnings("deprecation") // Processing of container.Tab is retained for compatibility and will be removed
    // in a version after 2.0.2
    public static Section from(com.exadel.aem.toolkit.api.annotations.container.Tab value, boolean isLayout) {
        if (value == null) {
            return null;
        }
        return new LegacyTabSection(value, isLayout);
    }

    /**
     * Creates a new {@link Section} instance for the given {@link AccordionPanel} instance
     * @param value    The {@code AccordionPanel} object to process
     * @param isLayout True if the current section is a dialog layout section; false if it is a dialog widget section
     * @return {@code Section} instance, or null in case of a null object passed as the argument
     */
    public static Section from(AccordionPanel value, boolean isLayout) {
        if (value == null) {
            return null;
        }
        return new AccordionPanelSection(value, isLayout);
    }

    /**
     * Creates a new {@link Section} instance for the given {@link Column} instance
     * @param value    The {@code Column} object to process
     * @param isLayout True if the current section is a dialog layout section; false if it is a dialog widget section
     * @return {@code Section} instance or null in case of a null object passed as the argument
     */
    public static Section from(Column value, boolean isLayout) {
        if (value == null) {
            return null;
        }
        return new ColumnSection(value, isLayout);
    }

    /**
     * Creates a new {@link Section} instance for the given title. This section will be designed in a way that it will
     * not be rendered in UI
     * @param title Title of the new section
     * @return {@code Section} instance
     */
    public static Section ignored(String title) {
        return new IgnoredSection(title);
    }
}
