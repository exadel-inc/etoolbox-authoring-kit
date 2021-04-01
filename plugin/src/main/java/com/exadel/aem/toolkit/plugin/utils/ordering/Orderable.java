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
package com.exadel.aem.toolkit.plugin.utils.ordering;

/**
 * Presents an abstraction of an entity able to be managed by an ordering routine
 * @param <T> Type of the entity
 * @see OrderingUtil
 */
class Orderable<T> {

    private final String name;
    private final T value;

    private Orderable<T> before;
    private Orderable<T> after;

    private int positionInAllNodes;

    /**
     * Initializes a class instance with the name and payload value provided
     * @param name  Name of the instance
     * @param value The wrapped object (payload)
     */
    public Orderable(String name, T value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Retrieves the name associated with this instance
     * @return String value
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the {@code before} reference hint associated with this instance
     * @return {@code Orderable} object
     */
    public Orderable<T> getBefore() {
        return before;
    }

    /**
     * Assigns to the current instance an {@code Orderable} object that would represent its {@code before} hint
     */
    void setBefore(Orderable<T> before) { // package-friendly setter for test cases
        this.before = before;
    }

    /**
     * Retrieves the {@code after} reference hint associated with this instance
     * @return {@code Orderable} object
     */
    public Orderable<T> getAfter() {
        return after;
    }

    /**
     * Assigns to the current instance an {@code Orderable} object that would represent its {@code after} hint
     */
    public void setAfter(Orderable<T> after) { // package-friendly setter for test cases
        this.after = after;
    }

    /**
     * Retrieves the payload associated with this instance
     * @return {@code T}-typed object
     */
    public T getValue() {
        return value;
    }

    /**
     * Retrieves the position of the current object in an ordered collection
     * @return Integer value
     */
    public int getPosition() {
        return positionInAllNodes;
    }

    /**
     * Sets the position of the current object in an ordered collection
     */
    public void setPosition(int positionInAllNodes) {
        this.positionInAllNodes = positionInAllNodes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        @SuppressWarnings("unchecked")
        Orderable<T> orderable = (Orderable<T>) o;

        return name.equals(orderable.name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
