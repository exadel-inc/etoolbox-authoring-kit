/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package com.exadel.aem.toolkit.api.annotations.main;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.container.AccordionPanel;
import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.meta.IgnorePropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyScope;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;
import com.exadel.aem.toolkit.api.annotations.widgets.common.Scope;

/**
 * Used to store generic properties of TouchUI Dialog and most common properties of AEM Component according to the
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/components-basics.html#PropertiesandChildNodesofaComponent"> Adobe specification</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@PropertyMapping
@SuppressWarnings("unused")
public @interface Dialog {
    /**
     * Maps to the 'jcr:title' attributes of both the component root node and its {@code cq:dialog} node
     * @return String value, non-blank
     */
    @PropertyRendering(name = JcrConstants.PN_TITLE)
    @ValueRestriction(ValueRestrictions.NOT_BLANK)
    String title();

    /**
     * When set to non-blank, maps to the 'jcr:description' attribute of the component's root node
     * @return String value
     */
    @PropertyRendering(name = JcrConstants.PN_DESCRIPTION)
    @PropertyScope(Scope.COMPONENT)
    @Deprecated
    String description() default "";

    /**
     * When set to non-blank, maps to the 'cq:cellName' attribute of the component's root node
     * @return String value
     */
    @PropertyRendering(name = JcrConstants.PN_CELL_NAME)
    @PropertyScope(Scope.COMPONENT)
    @Deprecated
    String cellName() default "";

    /**
     * When set to non-blank, maps to the 'componentGroup' attribute of the component root node
     * @return String value
     */
    @PropertyScope(Scope.COMPONENT)
    @Deprecated
    String componentGroup() default "";

    /**
     * Maps to the 'dialogPath' attribute of the component root node. Must represent a valid JCR path
     * @return String value
     */
    @PropertyScope(Scope.COMPONENT)
    @ValueRestriction(ValueRestrictions.JCR_PATH)
    @Deprecated
    String dialogPath() default "";

    /**
     * When set to true, renders as the 'cq:noDecoration' attribute of the component root node with `true` value
     * @return True or false
     */
    @PropertyRendering(name = JcrConstants.PN_NO_DECORATION, ignoreValues = "false")
    @PropertyScope(Scope.COMPONENT)
    @Deprecated
    boolean noDecoration() default false;

    /**
     * When set to non-blank, renders as the 'sling:resourceSuperType' attribute of the component root node
     * @return String value
     */
    @PropertyRendering(name = JcrConstants.PN_RESOURCE_SUPER_TYPE)
    @PropertyScope(Scope.COMPONENT)
    @Deprecated
    String resourceSuperType() default "";

    /**
     * Maps to the 'cq:templatePath' attribute of the component root node. Must represent a valid JCR path
     * @return String value
     */
    @PropertyRendering(name = JcrConstants.PN_TEMPLATE_PATH)
    @PropertyScope(Scope.COMPONENT)
    @ValueRestriction(ValueRestrictions.JCR_PATH)
    @Deprecated
    String templatePath() default "";

    /**
     * When set to non-blank, renders as the `helpPath` attribute of component's {@code cq:dialog} node
     * @return String value
     */
    @PropertyScope(Scope.CQ_DIALOG)
    String helpPath() default "";

    /**
     * Renders as the `height` attribute of component's {@code cq:dialog} node. If no value, or a value less or equal to zero provided, default 480 is used
     * @return Numeric value
     */
    @ValueRestriction(ValueRestrictions.POSITIVE)
    @PropertyScope(Scope.CQ_DIALOG)
    double height() default 480;

    /**
     * Renders as the `width` attribute of component's {@code cq:dialog} node. If no value, or a value less or equal to zero provided, default 560 is used
     * @return Numeric value
     */
    @ValueRestriction(ValueRestrictions.POSITIVE)
    @PropertyScope(Scope.CQ_DIALOG)
    double width() default 560;

    /**
     * When set to true, renders as the `disableTargeting` attribute of the component root node with `true` value
     * @return True or false
     * @deprecated Use {@link Component} to set this value
     */
    @PropertyScope(Scope.COMPONENT)
    @PropertyRendering(ignoreValues = "false")
    @Deprecated
    boolean disableTargeting() default false;

    /**
     * Renders as the 'isContainer' attribute of the component root node
     * @return True or false
     * @deprecated Use {@link Component} to set this value
     */
    @IgnorePropertyMapping
    @Deprecated
    boolean isContainer() default false;

    /**
     * Used to set path to the node/folder of component to create TouchUI dialog for. The path is relative to the {@code componentsPathBase}
     * config setting in package's POM file
     * @return String value
     */
    @IgnorePropertyMapping
    @ValueRestriction(ValueRestrictions.NOT_BLANK)
    String name() default "";

    /**
     * Used to specify TouchUI dialog layout
     * @return One of the FIXED_COLUMNS and TABS values
     * @see DialogLayout
     */
    @IgnorePropertyMapping
    DialogLayout layout() default DialogLayout.FIXED_COLUMNS;

    /**
     * When set to a non-blank String, or an array of strings, renders as the `extraClientlibs` attribute
     * of component's {@code cq:dialog} node
     * @return String value, or an array of strings
     */
    @PropertyScope(Scope.CQ_DIALOG)
    String[] extraClientlibs() default {};

    /**
     * For the tabbed TouchUI dialog layout, enumerates the tabs to be rendered
     * @return Zero or more {@code Tab} annotations
     * @see Tab
     */
    Tab[] tabs() default {};

    /**
     * For the accordion-shaped TouchUI dialog layout, enumerates the panels to be rendered
     * @return Zero or more {@code AccordionPanel} annotations
     * @see AccordionPanel
     */
    AccordionPanel[] panels() default {};
}
