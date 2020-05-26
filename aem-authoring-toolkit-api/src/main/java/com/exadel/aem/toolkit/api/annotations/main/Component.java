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

/**
 * Represents the back-end part of an AEM page component - an entity used to supply component metadata and
 * {@code Adobe Granite}-compliant XML markup for the associated component authoring tools to the component folder
 * in AEM content package.<br><br>
 * <p>
 * In a most basic case, the {@code @Component} is the Java class that defines a Sling model or a POJO
 * with the annotations to build an authoring {@code dialog} upon (particularly, the {@code @Dialog} annotation.<br><br>
 * <p>
 * Otherwise, a {@code @Component} may comprise a set of classes, each responsible for an aspect of the component's
 * presentation or behavior: a Sling model, a class to build {@code Dialog} view, a class to build {@code Design dialog} view,
 * etc. Each of these may belong to one particular component, or be reusable for a number
 * of components sharing similar parts of functionality
 *
 * @see Dialog
 * @see DesignDialog
 * @see com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {

    /**
     * The path to the folder of the current component in an AEM package,
     * relative to path specified in {@code componentsPathBase} configuration setting<br>
     * This property has precedence over the {@code name} property of a {@code Dialog} specified for the same component
     *
     * @return String value, non-blank
     */
    String path();

    /**
     * The set of views this {@code @Component} comprises. Each view represents a class which will be scanned for the
     * <b>AEM Authoring Toolkit</b> annotations. The current class is supposed by default, whether added to the set or not.
     *
     * @return Array {@code Class<?>} references, or an empty array
     */
    Class<?>[] views() default {};
}