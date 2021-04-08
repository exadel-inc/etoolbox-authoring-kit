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
package com.exadel.aem.toolkit.test.custom.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.meta.DialogWidgetAnnotation;
import com.exadel.aem.toolkit.api.annotations.meta.PropertyRendering;
import com.exadel.aem.toolkit.api.annotations.meta.ResourceType;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@DialogWidgetAnnotation(source = "testCustomAnnotation")
@ResourceType("test-components/form/customfield")
@SuppressWarnings({"unused", "deprecation"}) // @DialogWidgetAnnotation is retained for compatibility testing;
// will be removed in a version after 2.0.0
public @interface CustomWidgetAnnotation {

    @PropertyRendering(name = "custom")
    String customField() default "Non-overridden value";
}
