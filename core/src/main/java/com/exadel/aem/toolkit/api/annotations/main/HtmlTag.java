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

import com.day.cq.wcm.api.NameConstants;

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.Scopes;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

/**
 * Used to define decoration tag of an AEM component according to the
 * <a href="https://docs.adobe.com/content/help/en/experience-manager-65/developing/components/decoration-tag.html">Adobe specification</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(scope = Scopes.CQ_HTML_TAG)
public @interface HtmlTag {

    /**
     * Maps to the {@code class} attribute of the {@code cq:htmlTag} node
     * @return String value, non-blank
     */
    @PropertyRendering(name = "class")
    @ValueRestriction(ValueRestrictions.NOT_BLANK)
    String className();

    /**
     * Maps to the {@code cq:tagName} attribute of the {@code cq:htmlTag} node
     * @return String value, non-blank
     */
    @PropertyRendering(name = NameConstants.PN_TAG_NAME)
    @ValueRestriction(ValueRestrictions.NOT_BLANK)
    String tagName() default "div";
}
