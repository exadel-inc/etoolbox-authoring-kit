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

package com.exadel.aem.toolkit.api.annotations.widgets.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.exadel.aem.toolkit.api.annotations.meta.ValueRestriction;
import com.exadel.aem.toolkit.api.annotations.meta.ValueRestrictions;

@Retention(RetentionPolicy.RUNTIME)
public @interface OptionSource {

    @ValueRestriction(ValueRestrictions.NOT_BLANK)
    String path();

    String fallbackPath() default "";

    String textMember() default "";

    String valueMember() default "";

    String[] attributeMembers() default {};

    String[] attributes() default {};

    String textTransform() default "";

    String valueTransform() default "";

}
