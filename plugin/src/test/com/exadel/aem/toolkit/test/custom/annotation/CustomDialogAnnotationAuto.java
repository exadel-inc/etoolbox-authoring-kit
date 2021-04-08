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

import com.exadel.aem.toolkit.api.annotations.meta.AnnotationRendering;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AnnotationRendering(properties = "!field3")
// This will have its scope defined dynamically: when appended to a class already
// possessing e.g. @Dialog, it will have cq:dialog scope, etc.
public @interface CustomDialogAnnotationAuto {

    String field1() default "";

    long field2();

    boolean field3() default false; // will not be rendered
}
