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

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Represents the back-end part of an AEM component - an entity used to supply component metadata in accordance with
 * {@code Adobe Granite API}.<br><br>
 * <p>
 * In a most basic case, the {@code @Component} is the Java class that defines a Sling model or POJO
 * with the annotations to build an authoring dialog upon (particularly, the {@code @Dialog} annotation).<br><br>
 * <p>
 * Otherwise, a {@code @Component} may comprise a set of classes, each responsible for an aspect of the component's
 * presentation or behavior: a Sling model, a class to build {@code Dialog} view, a class to build {@code Design dialog}
 * view, etc. Each of these may belong to one particular component, or be reusable for a number of components sharing
 * similar parts of functionality
 * @see Dialog
 * @see DesignDialog
 * @see com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(properties = "!path")
public @interface AemComponent {

    /**
     * The path to the folder of the current AEM component in a package, either relative to the path specified in
     * {@code componentsPathBase} configuration setting, or absolute (that is, starting with {@code /}).
     * <br>If an absolute path is specified, it is considered starting from the folder situated under {@code jcr_root}
     * (such as {@code /apps/path/to/my/component}).
     * <br><br>This property has precedence over the {@code name} property of a {@code Dialog} specified for the same
     * component
     * @return String value, non-blank
     */
    String path();

    /**
     * The set of views this {@code @Component} comprises. Each view represents a class that will be scanned for the
     * ToolKit annotations. The current class is assumed by default, whether added to the set or not.
     * @return Array {@code Class<?>} references, or an empty array
     */
    Class<?>[] views() default {};

    /**
     * When set to a non-blank string, maps to the {@code jcr:description} attribute of the component's root node
     * @return String value
     */
    @PropertyRendering(name = JcrConstants.JCR_DESCRIPTION)
    String description() default "";

    /**
     * When set to non-blank, maps to the {@code cq:cellName} attribute of the component's root node
     * @return String value
     */
    @PropertyRendering(name = NameConstants.PN_CELL_NAME)
    String cellName() default "";

    /**
     * When set to a non-blank string, maps to the {@code componentGroup} attribute of the component root node
     * @return String value
     */
    String componentGroup() default "";

    /**
     * When set to a non-blank string, maps to the {@code dialogPath} attribute of the component root node. Must represent
     * a valid JCR path
     * @return String value
     */
    @ValueRestriction(ValueRestrictions.JCR_PATH)
    String dialogPath() default "";

    /**
     * When set to true, renders as the {@code cq:noDecoration} attribute of the component root node with `true` value
     * @return True or false
     */
    @PropertyRendering(
        name = NameConstants.PN_NO_DECORATION,
        ignoreValues = "false"
    )
    boolean noDecoration() default false;

    /**
     * When set to a non-blank string, renders as the {@code sling:resourceSuperType} attribute of the component root node
     * @return String value
     */
    @PropertyRendering(name = JcrResourceConstants.SLING_RESOURCE_SUPER_TYPE_PROPERTY)
    String resourceSuperType() default "";

    /**
     * Maps to the {@code cq:templatePath} attribute of the component root node. Must represent a valid JCR path
     * @return String value
     */
    @PropertyRendering(name = NameConstants.PN_TEMPLATE_PATH)
    @ValueRestriction(ValueRestrictions.JCR_PATH)
    String templatePath() default "";

    /**
     * When set to true, renders as the {@code disableTargeting} attribute of the component root node with `true` value
     * @return True or false
     */
    @PropertyRendering(ignoreValues = "false")
    boolean disableTargeting() default false;

    /**
     * Maps to the {@code jcr:title} attributes of the component root node
     * @return String value, non-blank
     */
    @PropertyRendering(name = JcrConstants.JCR_TITLE)
    @ValueRestriction(ValueRestrictions.NOT_BLANK)
    String title();

    /**
     * Renders as the {@code isContainer} attribute of the component root node
     * @return True or false
     */
    @PropertyRendering(
        name = NameConstants.PN_IS_CONTAINER,
        valueType = String.class,
        ignoreValues = "false"
    )
    boolean isContainer() default false;
}
