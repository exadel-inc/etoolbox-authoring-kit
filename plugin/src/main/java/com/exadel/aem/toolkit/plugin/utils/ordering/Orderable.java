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

class Orderable<T> {

    private final String name;

    private Orderable<T> before;

    private Orderable<T> after;

    private T value;

    private int positionInAllNodes;

    public Orderable(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Orderable<T> getBefore() {
        return before;
    }

    public void setBefore(Orderable<T> before) { // package-friendly setter for test cases
        this.before = before;
    }

    public Orderable<T> getAfter() {
        return after;
    }

    public void setAfter(Orderable<T> after) { // package-friendly setter for test cases
        this.after = after;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public int getPosition() {
        return positionInAllNodes;
    }

    public void setPosition(int positionInAllNodes) {
        this.positionInAllNodes = positionInAllNodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Orderable<T> orderable = (Orderable<T>) o;

        return name.equals(orderable.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
