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
package com.exadel.aem.toolkit.core.relay.models;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nonnull;

/**
 * Represents the set of data required to configure and operate a {@code RelayProvider}.
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class RelayInfo {

    private final RelayMapping pathMapping;

    private final Collection<RelayMapping> userMappings;

    private final Collection<ChangeSample> changeSamples;

    /**
     * Creates a new {@code RelayInfo} with the provided path mapping, user mappings and change samples
     * @param pathMapping   A non-null source-to-target mapping entry for path translation
     * @param userMappings  A collection of source-to-target mapping entries for user identity translation
     * @param changeSamples A collection of change announcement entries defining JCR paths or XPaths to report as
     *                      changed when the relay is enabled or disabled
     */
    public RelayInfo(
        @Nonnull RelayMapping pathMapping,
        Collection<RelayMapping> userMappings,
        Collection<ChangeSample> changeSamples) {
        this.pathMapping = pathMapping;
        this.userMappings = userMappings;
        this.changeSamples = changeSamples;
    }

    /**
     * Gets the source-to-target mapping entry for path translation
     * @return A non-null {@link RelayMapping} instance
     */
    @Nonnull
    public RelayMapping getPathMapping() {
        return pathMapping;
    }

    /**
     * Gets the collection of source-to-target mapping entries for user identity translation
     * @return A collection of {@link RelayMapping} instances
     */
    public Collection<RelayMapping> getUserMappings() {
        return userMappings;
    }

    /**
     * Gets the collection of change announcement entries defining JCR paths or XPaths to report as changed when the
     * relay is enabled or disabled
     * @return A collection of {@link ChangeSample} instances
     */
    public Collection<ChangeSample> getChangeSamples() {
        return Collections.unmodifiableCollection(changeSamples);
    }
}
