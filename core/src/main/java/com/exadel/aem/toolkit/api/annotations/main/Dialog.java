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
package com.exadel.aem.toolkit.api.annotations.main;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;

import com.exadel.aem.toolkit.api.annotations.container.Tab;
import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to store generic properties of a Granite UI Dialog and most common properties of an AEM Component according to the
 * <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/components-basics.html#PropertiesandChildNodesofaComponent">
 *     Adobe specification</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(
    properties = {"!name", "!layout"}
)
@SuppressWarnings("squid:S1133")
public @interface Dialog {

    /**
     * Maps to the {@code jcr:title} attributes of both the component root node and its {@code cq:dialog} node (unless
     * separate {@code title} is specified in {@link AemComponent} annotation for the current component)
     * @return String value, non-blank
     */
    @PropertyRendering(name = JcrConstants.JCR_TITLE)
    @ValueRestriction(ValueRestrictions.NOT_BLANK_OR_DEFAULT)
    String title() default "";

    /**
     * When set to a non-blank string, maps to the {@code jcr:description} attribute of the component's root node
     * @return String value
     * @deprecated Use {@link AemComponent#description()} to set this value
     */
    @PropertyRendering(
        name = JcrConstants.JCR_DESCRIPTION,
        scope = Scopes.COMPONENT
    )
    @Deprecated
    String description() default "";

    /**
     * When set to a non-blank string, maps to the {@code cq:cellName} attribute of the component's root node
     * @return String value
     * @deprecated Use {@link AemComponent#cellName()} to set this value
     */
    @PropertyRendering(
        name = NameConstants.PN_CELL_NAME,
        scope = Scopes.COMPONENT
    )
    @Deprecated
    String cellName() default "";

    /**
     * When set to a non-blank string, maps to the {@code componentGroup} attribute of the component root node
     * @return String value
     * @deprecated Use {@link AemComponent#componentGroup()} to set this value
     */
    @PropertyRendering(scope = Scopes.COMPONENT)
    @Deprecated
    String componentGroup() default "";

    /**
     * Maps to the {@code dialogPath} attribute of the component root node. Must represent a valid JCR path
     * @return String value
     * @deprecated Use {@link AemComponent#dialogPath()} to set this value
     */
    @PropertyRendering(scope = Scopes.COMPONENT)
    @ValueRestriction(ValueRestrictions.JCR_PATH)
    @Deprecated
    String dialogPath() default "";

    /**
     * When set to true, renders as the {@code cq:noDecoration} attribute of the component root node with `true` value
     * @return True or false
     * @deprecated Use {@link AemComponent#noDecoration()} to set this value
     */
    @PropertyRendering(
        name = NameConstants.PN_NO_DECORATION,
        scope = Scopes.COMPONENT,
        ignoreValues = "false"
    )
    @Deprecated
    boolean noDecoration() default false;

    /**
     * When set to a non-blank string, renders as the {@code sling:resourceSuperType} attribute of the component root node
     * @return String value
     * @deprecated Use {@link AemComponent#resourceSuperType()} to set this value
     */
    @PropertyRendering(
        name = JcrResourceConstants.SLING_RESOURCE_SUPER_TYPE_PROPERTY,
        scope = Scopes.COMPONENT
    )
    @Deprecated
    String resourceSuperType() default "";

    /**
     * Maps to the {@code cq:templatePath} attribute of the component root node. Must represent a valid JCR path
     * @return String value
     * @deprecated Use {@link AemComponent#templatePath()} to set this value
     */
    @PropertyRendering(
        name = NameConstants.PN_TEMPLATE_PATH,
        scope = Scopes.COMPONENT
    )
    @ValueRestriction(ValueRestrictions.JCR_PATH)
    @Deprecated
    String templatePath() default "";

    /**
     * When set to a non-blank string, renders as the `helpPath` attribute of component's {@code cq:dialog} node
     * @return String value
     */
    @PropertyRendering(scope = Scopes.CQ_DIALOG)
    String helpPath() default "";

    /**
     * Renders as the {@code height} attribute of component's {@code cq:dialog} node. If no value provided,
     * default {@code 480} is used
     * @return Double-typed number
     */
    @PropertyRendering(scope = Scopes.CQ_DIALOG)
    @ValueRestriction(ValueRestrictions.POSITIVE)
    double height() default 480;

    /**
     * Renders as the {@code width} attribute of component's {@code cq:dialog} node. If no value provided,
     * default {@code 560} is used
     * @return Double-typed number
     */
    @PropertyRendering(scope = Scopes.CQ_DIALOG)
    @ValueRestriction(ValueRestrictions.POSITIVE)
    double width() default 560;

    /**
     * When set to true, renders as the {@code disableTargeting} attribute of the component root node with its value
     * set to "true"
     * @return True or false
     * @deprecated Use {@link AemComponent#disableTargeting()} to set this value
     */
    @PropertyRendering(
        scope = Scopes.COMPONENT,
        ignoreValues = "false"
    )
    @Deprecated
    boolean disableTargeting() default false;

    /**
     * Renders as the {@code isContainer} attribute of the component root node
     * @return True or false
     * @deprecated Use {@link AemComponent#isContainer()} to set this value
     */
    @PropertyRendering(
        scope = Scopes.COMPONENT,
        name = NameConstants.PN_IS_CONTAINER,
        valueType = String.class,
        ignoreValues = "false"
    )
    @Deprecated
    boolean isContainer() default false;

    /**
     * Used to set path to the node of component for which to create Granite UI entities, such as the {@code cq:dialog}
     * node. The path is relative to the {@code componentsPathBase} config setting
     * @return String value
     * @deprecated Use {@link AemComponent#path()} to set this value
     */
    @ValueRestriction(ValueRestrictions.NOT_BLANK)
    @Deprecated
    String name() default "";

    /**
     * Used to specify a Granite UI dialog layout
     * @return One of the FIXED_COLUMNS, TABS, or ACCORDION values
     * @see DialogLayout
     * @deprecated This property is no longer needed. The proper layout is set based on the container annotations,
     * such as {@code Tabs} or {@code Accordion} added directly to the dialog class or one of its superclasses
     */
    @Deprecated
    DialogLayout layout() default DialogLayout.FIXED_COLUMNS;

    /**
     * When set to a non-blank string or array of strings, renders as the {@code extraClientlibs} attribute
     * of component's {@code cq:dialog} node
     * @return String value, or an array of strings
     */
    @PropertyRendering(scope = Scopes.CQ_DIALOG)
    String[] extraClientlibs() default {};

    /**
     * For the tabbed Granite UI layout, enumerates the tabs to be rendered
     * @return Zero or more {@code Tab} annotations
     * @see Tab
     * @deprecated This property is for compatibility purposes. It's recommended that you add {@code @Tabs} annotation
     * to your dialog class instead
     */
    @Deprecated
    Tab[] tabs() default {};
}
