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

import com.exadel.aem.toolkit.api.handlers.Target;

/**
 * Contains factory methods for creating {@link Target} instances
 */
public class Targets {

    /**
     * Default (instantiation-restricting) constructor
     */
    private Targets() {
    }

    /**
     * Creates a new unattached (root) {@code Target} instance with the specified name
     * @param name Name of the instance, non-blank String
     * @return {@code Target} object
     */
    public static Target newInstance(String name) {
        return new TargetImpl(name, null);
    }

    /**
     * Creates a new unattached (root) {@code Target} instance with the specified name and scope
     * @param name  Name of the instance, non-blank String
     * @param scope Scope of the instance, see {@link com.exadel.aem.toolkit.api.annotations.meta.Scopes} for details
     * @return {@code Target} object
     */
    public static Target newInstance(String name, String scope) {
        TargetImpl result = new TargetImpl(name, null);
        result.setScope(scope);
        return result;
    }

    /**
     * Creates a new unattached {@code Target} instance with the specified name and parent. Unlike
     * {@link Target#createTarget(String)}, this method will not automatically add the newly created instance to the
     * parent's collection
     * @param name   Name of the instance, non-blank String
     * @param parent Reference to a parent {@code Target} instance
     * @return {@code Target} object
     */
    public static Target newInstance(String name, Target parent) {
        return new TargetImpl(name, parent);
    }

}
