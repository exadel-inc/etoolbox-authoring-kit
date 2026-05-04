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

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a source-to-target mapping entry used for path or user identity translation. Mappings are designed so that
 * a set of mappings can only contain entries with different sources ({@code from}-s) to avoid ambiguity as to which
 * mapping to apply for a given source value.
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class RelayMapping {

    private final String from;
    private final String to;

    /**
     * Creates a new {@code Mapping} with the provided source and target values
     * @param from Source value
     * @param to   Target value
     */
    @JsonCreator
    RelayMapping(@JsonProperty("from") String from, @JsonProperty("to") String to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Gets the source value of this mapping
     * @return A nullable source string
     */
    public String getFrom() {
        return from;
    }

    /**
     * Gets the target value of this mapping
     * @return A nullable target string
     */
    public String getTo() {
        return to;
    }

    /**
     * Gets whether the mapping has non-blank source and target values
     * @return True or false
     */
    public boolean isValid() {
        return StringUtils.isNoneBlank(getFrom(), getTo());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof RelayMapping)) {
            return false;
        }
        RelayMapping mapping = (RelayMapping) other;
        return Objects.equals(from, mapping.from);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(from);
    }
}
