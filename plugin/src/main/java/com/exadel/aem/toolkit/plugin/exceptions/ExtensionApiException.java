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

package com.exadel.aem.toolkit.plugin.exceptions;

/**
 * Represents the plugin-specific exception due to failure in instantiating and/or calling an extension API feature,
 * such as a custom handler
 */
public class ExtensionApiException extends RuntimeException {
    public ExtensionApiException(Class<?> customType, Exception cause) {
        super("Could not invoke " + customType.getName(), cause);
    }
}
