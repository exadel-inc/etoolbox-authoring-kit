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
package com.exadel.aem.toolkit.plugin.metadata.scripting;

import org.mozilla.javascript.ScriptableObject;

/**
 * Represents a basic object used by the {@code Rhino} engine to expose Java objects leveraging reflective operations to
 * inline scripts
 * @see ScriptingHelper
 */
abstract class AbstractAdapter extends ScriptableObject {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassName() {
        return getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        if (String.class.equals(typeHint)) {
            return getClassName();
        }
        return super.getDefaultValue(typeHint);
    }
}
