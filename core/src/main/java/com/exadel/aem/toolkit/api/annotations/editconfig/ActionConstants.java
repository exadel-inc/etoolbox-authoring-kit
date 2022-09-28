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

/**
 * Contains most common string values for the {@link EditConfig#actions()} property
 */
public class ActionConstants {
    public static final String EDIT = "edit";
    public static final String DELETE = "delete";
    public static final String INSERT = "insert";
    public static final String COPYMOVE = "copymove";

    /**
     * Default (instantiation-preventing) constructor
     */
    private ActionConstants() {}
}
