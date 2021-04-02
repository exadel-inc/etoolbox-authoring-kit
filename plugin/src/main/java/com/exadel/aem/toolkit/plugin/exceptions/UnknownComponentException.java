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

import java.nio.file.Path;

/**
 * Represents the plugin-specific exception thrown when a processable Java component does not match a component folder
 * in the package
 */
public class UnknownComponentException extends RuntimeException {

    /**
     * Initializes a class instance with the component's path specified
     * @param value Non-null {@code Path} object
     */
   public UnknownComponentException(Path value) {
        super(String.format("Component at %s not present in the package", value));
    }
}
