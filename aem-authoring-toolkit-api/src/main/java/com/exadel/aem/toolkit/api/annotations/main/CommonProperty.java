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

import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyScope;

/**
 * Used to populate {@link CommonProperties} value with set of name-value string pairs. They are rendered as XML nodes' attributes
 * To define appropriate XML node, {@link XmlScope} and node's {@code XPath} can be specified
 * @see CommonProperties
 * @see PropertyScope
 */
@SuppressWarnings("unused")
public @interface CommonProperty {
    /**
     * Indicates the attribute name
     * @return String value, non-blank
     */
    String name();
    /**
     * Indicates the attribute value
     * @return String value
     */
    String value();
    /**
     * Specifies the scope to deal with, i.e. whether
     * this attribute is rendered to {@code cq:Component} (component root), {@code ca:dialog}, or {@code cq:editorConfig} JCR nodes
     * @return One of {@code XmlScope} values
     */
    XmlScope scope() default XmlScope.COMPONENT;
    /**
     * Specifies the path to target node relative to node defined by {@link CommonProperty#scope()}
     * @return String representing JCR path, namespace-agnostic (i.e. 'editConfig' instead of 'cq:editConfig', etc.)
     */
    String path() default "/";
}
