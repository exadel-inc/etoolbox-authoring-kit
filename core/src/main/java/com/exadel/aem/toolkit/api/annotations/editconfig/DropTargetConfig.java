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
package com.exadel.aem.toolkit.api.annotations.editconfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;

/**
 * Defines a target for a drag-and-drop operation in Granite UI. Upon processing this annotation,
 * a {@code cq:editConfig/cq:dropTargets/[targetName]} node within a component's buildup is created
 * See <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/components-basics.html#main-pars_title_2_tckaux_refd_">AEM Components documentation</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(properties = "propertyName")
public @interface DropTargetConfig {

    /**
     * Maps to the {@code accept} attribute of {@code cq:editConfig/cq:dropTargets/<targetName>} node
     * @return Non-blank string, or an array of strings
     */
    String[] accept() default {};

    /**
     * Maps to the {@code groups} attribute of {@code cq:editConfig/cq:dropTargets/<targetName>} node
     * @return Non-blank string, or an array of strings
     */
    String[] groups() default {};

    /**
     * Maps to the {@code propertyName} attribute of {@code cq:editConfig/cq:dropTargets/<targetName>} node
     * @return Non-blank string
     */
    String propertyName();

    /**
     * Used to specify tag name of the current {@code cq:editConfig/cq:dropTargets} subnode
     * @return Non-blank string
     */
    String nodeName();
}
