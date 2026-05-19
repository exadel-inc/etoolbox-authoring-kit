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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a change announcement entry defining a JCR path or XPath to report as changed when the relay is enabled or
 * disabled.
 * <p><u>Note</u>: This class is not a part of the public API and is subject to change. Do not use it in your own
 * code</p>
 */
public class ChangeSample {
    private final String path;
    private final int limit;
    private final String user;

    /**
     * Creates a new {@code ChangeSample} with the provided path, limit and user values
     * @param path  JCR path or XPath expression to report as changed
     * @param limit Optional limit of resources to report as changed under the provided path
     * @param user  Optional user identifier to use for reporting changes
     */
    @JsonCreator
    ChangeSample(
        @JsonProperty("path") String path,
        @JsonProperty("limit") int limit,
        @JsonProperty("user") String user) {
        this.path = path;
        this.limit = limit;
        this.user = user;
    }

    /**
     * Gets the JCR path or XPath expression defined in this announcement
     * @return String value
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the optional limit of resources to report as changed under the provided path
     * @return Integer limit value
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Gets the optional user identifier to use for reporting changes
     * @return A nullable user identifier string
     */
    public String getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     * <p>The equality check is based solely on the {@code path} property since we do not want to report the
     * same path as changed multiple times</p>
     */
    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof ChangeSample)) {
            return false;
        }
        ChangeSample announcement = (ChangeSample) other;
        return Objects.equals(path, announcement.path);
    }

    /**
     * {@inheritDoc}
     * <p>The hash code is based solely on the {@code path} property since we do not want to report the same path as
     * changed multiple times</p>
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }
}
