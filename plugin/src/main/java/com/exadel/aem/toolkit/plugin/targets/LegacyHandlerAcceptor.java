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
package com.exadel.aem.toolkit.plugin.targets;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.handlers.Target;

/**
 * Extends {@link Target} to provide the ability to consume attributes passed as the content of a DOM {@code Element}.
 * This is used to facilitate the functionality of legacy custom handlers that work with {@code Element} objects.
 * The feature will be retired as soon as support for legacy handlers ends
 */
public interface LegacyHandlerAcceptor {

    /**
     * Assigns attributes to the current instance based on the provided DOM {@code Element} object
     * @param value {@code Element} object used as the source of attribute names and values
     * @return Current instance
     */
    Target attributes(Element value);
}
