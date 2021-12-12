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
package com.exadel.aem.toolkit.plugin.handlers.placement.registries;

import java.util.List;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.handlers.Source;

/**
 * Collects and manages information on Java class members that can be rendered in a particular container, such as a
 * dialog with its tabs or panels, or else an in-dialog container widget. This registry is designed to be shared among
 * various handlers to avoid rendering the same member several times
 */
public class MembersRegistry {

    private final MembersRegistry upstream;
    private final List<Entry> entries;

    /**
     * Creates a new independent registry instance
     * @param members List of additional {@code Source} objects representing class members
     */
    public MembersRegistry(List<Source> members) {
        this(null, members);
    }

    /**
     * Creates a new connected registry instance. This one will respect an "upstream" registry and members stored in it,
     * and will take account of additional members (e.g. added to a container "from outside" in a later stage of
     * rendering (via {@code @Place} or a similar mechanism).
     * <p>The gist of using an "upstream" registry is in reporting from a handler that processes an in-dialog container,
     * such as Tabs or Accordion,
     * to the handler that does the overall dialog layout</p>
     * @param upstream {@link MembersRegistry} instance that stores the basic set of class members
     * @param members  List of additional ("local") {@code Source} objects representing class members
     */
    public MembersRegistry(MembersRegistry upstream, List<Source> members) {
        this.upstream = upstream;
        this.entries = members
            .stream()
            .map(Entry::new)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of members currently available for placement (those that have not been already placed in a
     * container)
     * @return List of {@code Source}s representing class members; possibly an empty list
     */
    public List<Source> getAvailable() {
        return entries
            .stream()
            .filter(entry -> entry.getState() == EntryState.AVAILABLE)
            .map(Entry::getMember)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of members currently available for placement (those that have not been already placed and also
     * those that have been conditionally placed in a top-level container with a possibility to transfer to a nested
     * one)
     * @return List of {@code Source}s representing class members; possibly an empty list
     */
    public List<Source> getAllAvailable() {
        return entries
            .stream()
            .filter(entry -> entry.getState() == EntryState.AVAILABLE || entry.getState() == EntryState.SOFT_CHECKED_OUT)
            .map(Entry::getMember)
            .collect(Collectors.toList());
    }

    /**
     * Certifies that the given member has been placed in a container and is not available for further placement
     * @param member {@code Source} object that represents a class member
     */
    public void checkOut(Source member) {
        if (upstream != null) {
            upstream.checkOut(member);
        }
        entries
            .stream()
            .filter(entry -> entry.getMember().equals(member))
            .findFirst()
            .ifPresent(entry -> entry.setState(EntryState.CHECKED_OUT));
    }

    /**
     * Certifies that the given member has been placed in a top-level container and is generally not available for
     * further placement unless in a container that perfectly matches the member's {@code @Place} directive
     * @param member {@code Source} object that represents a class member
     */
    public void softCheckOut(Source member) {
        if (upstream != null) {
            upstream.softCheckOut(member);
        }
        entries
            .stream()
            .filter(entry -> entry.getMember().equals(member))
            .findFirst()
            .ifPresent(entry -> entry.setState(EntryState.SOFT_CHECKED_OUT));
    }

    /* ---------------
       Utility classes
       --------------- */

    /**
     * Represents a storage entry of this {@link MembersRegistry}. An entry unites a {@code Source} object that refers
     * to a class member and an {@link EntryState} value manifesting the state
     */
    private static class Entry {
        private final Source member;
        private EntryState state;

        /**
         * Initializes this instance with the given {@code Source} object and the default state
         * @param member {@code Source} object referring a class member; a non-null value is expected
         */
        public Entry(Source member) {
            this.member = member;
            this.state = EntryState.AVAILABLE;
        }

        /**
         * Retrieves the stored {@code Source}
         * @return {@code Source} object
         */
        public Source getMember() {
            return member;
        }

        /**
         * Retrieves the state of the stored {@code Source}
         * @return {@code EntryState} value
         */
        public EntryState getState() {
            return state;
        }

        /**
         * Assigns the state of the current {@code Source}
         * @param state {@code EntryState} value
         */
        public void setState(EntryState state) {
            this.state = state;
        }
    }

    /**
     * Provides possible states of a {@code MemberRegistry}'s {@link Entry}
     */
    private enum EntryState {
        AVAILABLE, SOFT_CHECKED_OUT, CHECKED_OUT;
    }
}
