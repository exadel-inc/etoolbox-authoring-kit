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
package com.exadel.aem.toolkit.api.annotations.editconfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.exadel.aem.toolkit.api.annotations.meta.PropertyMapping;

/**
 * Defines a target for a drag-and-drop operation in a TouchUI dialog. Upon properties of this annotation,
 * a {@code cq:editConfig/cq:dropTargets/[targetName]} node within a component's buildup is created
 * See <a href="https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/components-basics.html#main-pars_title_2_tckaux_refd_">AEM Components documentation</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@PropertyMapping(mappings = "propertyName")
public @interface DropTargetConfig {

    /**
     * Maps to the 'accept' attribute of {@code cq:editConfig/cq:dropTargets/[targetName]} node
     * @return Non-blank string, or an array of strings
     */
    String[] accept() default {};

    /**
     * Maps to the 'groups' attribute of {@code cq:editConfig/cq:dropTargets/[targetName]} node
     * @return Non-blank string, or an array of strings
     */
    String[] groups() default {};

    /**
     * Maps to the 'propertyName' attribute of {@code cq:editConfig/cq:dropTargets/[targetName]} node
     * @return Non-blank string
     */
    String propertyName();

    /**
     * Used to specify tag name of the current {@code cq:editConfig/cq:dropTargets} subnode
     * @return Non-blank string
     */
    String nodeName();
}
