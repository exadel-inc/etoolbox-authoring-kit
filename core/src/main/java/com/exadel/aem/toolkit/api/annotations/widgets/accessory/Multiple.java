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
package com.exadel.aem.toolkit.api.annotations.widgets.accessory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.widgets.TextField;

/**
 * When appended to a field that has a valid widget annotation
 * (such as {@link TextField}, {@link com.exadel.aem.toolkit.api.annotations.widgets.select.Select}, etc.)
 * indicates that an arbitrary array of widgets of this type should be rendered instead of a singular widget.
 * <br><br>
 * Technically, for a statement like "{@code @SomeWidget @Multiple private String field... }", there will be rendered
 * a {@code Multifield} that can contain an arbitrary number of {@code SomeWidget}s
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Multiple {
}
