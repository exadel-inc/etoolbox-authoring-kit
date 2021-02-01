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

package com.exadel.aem.toolkit.plugin.util.ordering;

class Orderable {

    private final String name;

    private Orderable before;

    private Orderable after;

    private Object value;

    private int positionInAllNodes;

    private Orderable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Orderable getBefore() {
        return before;
    }

    public void setBefore(Orderable before) { // package-friendly setter for test cases
        this.before = before;
    }

    public Orderable getAfter() {
        return after;
    }

    public void setAfter(Orderable after) { // package-friendly setter for test cases
        this.after = after;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getPositionInAllNodes() {
        return positionInAllNodes;
    }

    public void setPositionInAllNodes(int positionInAllNodes) {
        this.positionInAllNodes = positionInAllNodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Orderable orderable = (Orderable) o;

        return name.equals(orderable.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static Orderable from(String name) { // package-friendly factory method for test cases
        return new Orderable(name);
    }
}
