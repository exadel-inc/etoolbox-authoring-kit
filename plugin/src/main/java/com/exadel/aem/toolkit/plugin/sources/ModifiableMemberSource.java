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
package com.exadel.aem.toolkit.plugin.sources;

import java.lang.reflect.Member;

import com.exadel.aem.toolkit.api.handlers.EmbeddedMemberSource;
import com.exadel.aem.toolkit.api.handlers.MemberSource;
import com.exadel.aem.toolkit.api.handlers.Source;

/**
 * Extends the {@code MemberSource} with methods that allow modifying ("overlaying") the reported properties of Java
 * classes' fields and methods
 * @see Source
 * @see MemberSource
 */
public interface ModifiableMemberSource extends EmbeddedMemberSource {

    /**
     * Stores the string that is used as the "overriding" name of the current {@code Source}
     * @param value Non-blank string
     */
    void setName(String value);

    /**
     * Assigns the {@code Class} that the underlying Java field or method will be considered belonging to. The routine
     * is mostly used for implementing member "replacing"/"moving" logic
     * @param value {@code Class} reference
     */
    void setDeclaringClass(Class<?> value);

    /**
     * Assigns the {@code Class} that the underlying Java field or method will be considered "reported by". The routine
     * is used for placing ToolKit widgets in proper containers
     * @param value {@code Class} reference
     */
    void setReportingClass(Class<?> value);

    /**
     * Assigns the {@link Member} value that the underlying Java field or method will be considered "reported by". This
     * facility is designed for members of entities such as fieldsets. This method gives the ability to query for
     * metadata attached to the field/method of a class that uses the fieldset
     * @param value {@code Member} reference
     */
    void setUpstreamMember(Member value);
}
