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

package com.exadel.aem.toolkit.plugin.source;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import com.exadel.aem.toolkit.api.handlers.Source;

/**
 * Factory class used to create {@link Source} instances
 */
public class Sources {

    /**
     * Default hiding (private) constructor
     */
    private Sources() {
    }

    /**
     * Creates a {@link Source} facade for a Java class member
     * @param member A {@code Method} or a {@code Field} for which a source facade is cledayed
     * @return {@code Source instance}
     */
    public static Source fromMember(Member member) {
        return fromMember(member, null);
    }

    /**
     * Creates a {@link Source} facade for a Java class member
     * @param member A {@code Method} or a {@code Field} for which a source facade is cledayed
     * @param reportingClass {@code Class<?>} pointer determing the class that "reports" the AEM Authpring Toolkit's plugin
     *                       of current member (can be a class where this member was declared or a descendant of
     *                       some superclass that uses the memeber for UI rendering)
     * @return {@code Source instance}
     */
    public static Source fromMember(Member member, Class<?> reportingClass) {
        return member instanceof Field
            ? new FieldSourceImpl((Field) member, reportingClass)
            : new MethodSourceImpl((Method) member, reportingClass);
    }
}
