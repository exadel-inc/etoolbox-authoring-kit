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
package com.exadel.aem.toolkit.api.handlers;

import java.lang.reflect.Member;

public interface EmbeddedMemberSource extends MemberSource {
    /**
     * Retrieves an optional {@code Member} reference pointing to a member of a foreign Java class that triggered
     * rendering of the class that contains the current member. This is useful for rendering containers such as {@code
     * FieldSet}s.
     * <p><i>Ex.: Class named "{@code Foo}" contains the field {@code private FooFieldset fooFieldset;}. Class named
     * "{@code FooFieldset}" contains the field {@code private String bar}. As the plugin renders markup for {@code
     * Foo}, it needs to render members of {@code FooFieldset} inside. As it reaches the field map {@code bar}, it will
     * use the value {@code reportingClass = Foo.class} and grab values from {@code Foo} if needs to</i></p>
     * @return A nullable {@code Member} reference
     */
    Member getUpstreamMember();
}
