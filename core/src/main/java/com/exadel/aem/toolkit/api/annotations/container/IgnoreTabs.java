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
package com.exadel.aem.toolkit.api.annotations.container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.exadel.aem.toolkit.api.annotations.widgets.accessory.Ignore;

/**
 * Used to specify tabs that are ignored while rendering XML markup for the current dialog. Typically, used
 * when current dialog class extends another class exposing one or more {@link Tab}s that are not currently needed
 * @deprecated This is deprecated and will be removed in a version after 2.0.2. Please use {@link Ignore} and fill in
 * its {@code sections} value instead
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
@SuppressWarnings("squid:S1133")
public @interface IgnoreTabs {

    /**
     * Enumerates the fields to be skipped from rendering for the current dialog, each specified by its title
     * @return One or more {@code String} values, non-blank
     */
    String[] value();
}
