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
package com.exadel.aem.toolkit.api.annotations.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;

/**
 * Used to specify one or more {@link XmlScope}s this annotation or annotation property is rendered in, i.e. whether
 * this is rendered to {@code cq:Component} (component root), {@code ca:dialog}, or {@code cq:editorConfig} JCR nodes.
 * Mind that it applies only to values that technically may be rendered to multiple JCR nodes,
 * such as {@link com.exadel.aem.toolkit.api.annotations.main.Dialog} annotation properties
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface PropertyScope {
    /**
     * Defines valid {@code XmlScope} or scopes
     * @return Array of {@link XmlScope} values
     */
    XmlScope[] value() default {XmlScope.COMPONENT, XmlScope.CQ_DIALOG, XmlScope.CQ_EDIT_CONFIG};
}
