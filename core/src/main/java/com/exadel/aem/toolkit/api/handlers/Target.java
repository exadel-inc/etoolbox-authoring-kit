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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Presents an abstraction of target entity for rendering AEM components and their configuration. Technically each
 * {@code Target} instance represents a future Granite UI entity, or a corresponding XML node. The {@code Target} can have
 * its attributes, a parent target and children in the same way as Granite/XML nodes do
 */
public interface Target {

    /**
     * Retrieves the name of the current {@code Target} instance
     * @return String value
     */
    String getName();

    /**
     * Retrieves the name prefix associated with the current instance
     * @return String value
     */
    String getNamePrefix();

    /**
     * Assigns the name prefix to the current instance. Call to this method can be trailed by other method calls
     * @param prefix String value, non-blank string expected
     * @return Current instance
     */
    Target namePrefix(String prefix);

    /**
     * Retrieves the name postfix associated with the current instance
     * @return String value
     */
    String getNamePostfix();

    /**
     * Assigns the name postfix to the current instance. Call to this method can be trailed by other method calls
     * @param postfix String value, non-blank string expected
     * @return Current instance
     */
    Target namePostfix(String postfix);

    /**
     * Retrieves the parent of the current {@code Target} instance
     * @return Another {@code Target} instance, or null if the current target has no parents (is at the top of targets tree)
     */
    Target getParent();

    /**
     * Retrieves the collection of {@code Target} instances attached to the current instance. Each one of the children
     * would report the current {@code Target} as its parent
     * @return An ordered (linked) {@code List} object. If there are no children, an empty non-null list is returned
     */
    List<Target> getChildren();

    /**
     * Gets whether the current {@code Target} is in a non-initialized state. The returned value is "true" if the current
     * object has no attributes except for the necessary {@code primaryType} attribute, and no children
     * @return True or false
     */
    boolean isEmpty();

    /**
     * Retrieves the scope this {@code Target} is associated with
     * @return String value
     * @see com.exadel.aem.toolkit.api.annotations.meta.Scopes
     */
    String getScope();

    /**
     * Gets whether the specified path leads to a valid {@code Target} object associated with the current instance.
     * The path is resolved in a generic XML/filesystem style like {@code ./current/child/grandchild/../../child-sibling}
     * @param path String value, non-blank
     * @return True or false
     */
    default boolean exists(String path) {
        return getTarget(path) != null;
    }

    /**
     * Retrieves a {@code Target} object associated with the current instance through the path specified.
     * The path is resolved in a generic XML/filesystem style like {@code ./current/child/grandchild/../../child-sibling}
     * @param path String value, non-blank
     * @return Valid {@code Target} object or null
     */
    Target getTarget(String path);

    /**
     * Retrieves a {@code Target} object associated with the current instance through the path specified.
     * The path is resolved in a generic XML/filesystem style like {@code ./current/child/grandchild/../../child-sibling}.
     * If a valid relative target cannot be found, a new {@code Target} is created for every missing segment of the path.
     * The exclusion is the missing parent path: if an object's parent does not exist, the current object is accounted
     * @param path String value, non-blank
     * @return Valid {@code Target} object or null
     */
    Target getOrCreateTarget(String path);

    /**
     * Makes sure that a {@code Target} object resolvable from the current instance through the given path is created.
     * The path is resolved in a generic XML/filesystem style like {@code ./current/child/grandchild/../../child-sibling}.
     * If a valid relative target cannot be found, a new {@code Target} is created for every missing segment of the path.
     * If the target matching the terminal segment of the path already exists, a new sibling is created and given a unique
     * name
     * @param path String value, non-blank
     * @return Valid {@code Target} object or null
     */
    Target createTarget(String path);


    /**
     * Appends an existing {@code Target} object as a child of the current instance. If the other {@code Target} has been
     * attached to another parent, its parent is swapped for the current instance
     * @param other Non-null {@code Target} object
     */
    default void addTarget(Target other) {
        addTarget(other, getChildren().size());
    }

    /**
     * Adds an existing {@code Target} object as a child of the current instance. If the other {@code Target} has been
     * attached to another parent, its parent is swapped for the current instance. The new child is placed before the
     * specified existing child
     * @param other Non-null {@code Target} object
     * @param near  Existing child {@code Target} of the current instance
     */
    default void addTarget(Target other, Target near) {
        addTarget(other, near, false);
    }

    /**
     * Adds an existing {@code Target} object as a child of the current instance. If the other {@code Target} has been
     * attached to another parent, its parent is swapped for the current instance. The new child is placed either before
     * or after the specified existing child
     * @param other      Non-null {@code Target} object
     * @param near       Existing child {@code Target} of the current instance
     * @param placeAfter True to insert the new child after the specified sibling. Otherwise, it will be inserted before
     *                   the existing sibling
     */
    default void addTarget(Target other, Target near, boolean placeAfter) {
        int position = near != null ? getChildren().indexOf(near) : -1;
        if (placeAfter) {
            position++;
        }
        addTarget(other, position);
    }

    /**
     * Adds an existing {@code Target} object as a child of the current instance. If the other {@code Target} has been
     * attached to another parent, its parent is swapped for the current instance. The new child is placed at the specified
     * position
     * @param other    Non-null {@code Target} object
     * @param position Zero-based position in the list of existing children
     */
    void addTarget(Target other, int position);

    /**
     * Removes an existing {@code Target} related to the current instance through the given path
     * @param path String value, non-blank
     */
    void removeTarget(String path);

    /**
     * Traverses upwards the line of parents of the current {@code Target} until an instance matching the given
     * predicate is found
     * @param filter Non-null {@code Predicate} to test the ancestors of the current target
     * @return {@code Target} object matching the criteria, or null
     */
    Target findParent(Predicate<Target> filter);

    /**
     * Traverses downwards the line of children of the current {@code Target} until an instance matching the given
     * predicate is found
     * @param filter Non-null {@code Predicate} to test the descendants of the current target
     * @return {@code Target} object matching the criteria, or null
     */
    Target findChild(Predicate<Target> filter);

    /**
     * Traverses downwards the line of children of the current {@code Target} and collects the instances that meet the
     * given criteria
     * @param filter Non-null {@code Predicate} to test against the descendants of the current target
     * @return {@code List} of targets matching the criteria. If none found, a non-null empty list is returned
     */
    List<Target> findChildren(Predicate<Target> filter);

    /**
     * Retrieves the attributes of the current instance
     * @return {@code Map} object containing string-typed name-value pairs
     */
    Map<String, String> getAttributes();

    /**
     * Retrieves an attribute of the current instance specified by name
     * @param name Name of the attribute, non-null string
     * @return Nullable string value
     */
    default String getAttribute(String name) {
        return getAttributes().get(name);
    }

    /**
     * Retrieves an attribute of the current instance specified by name
     * @param name         Name of the attribute, non-null string
     * @param defaultValue The value to return if an attribute with such a name is not found
     * @return Nullable string value
     */
    default String getAttribute(String name, String defaultValue) {
        return getAttributes().getOrDefault(name, defaultValue);
    }

    /**
     * Sets a string attribute of the current instance. Call to this method can be trailed by other method calls
     * @param name  Name of the attribute, non-null string
     * @param value Value of the attribute
     * @return Current instance
     */
    Target attribute(String name, String value);

    /**
     * Sets an attribute of the current instance as an array of strings. Call to this method can be trailed by other
     * method calls
     * @param name  Name of the attribute, non-null string
     * @param value Value of the attribute
     * @return Current instance
     */
    Target attribute(String name, String[] value);

    /**
     * Sets a boolean attribute of the current instance. Call to this method can be trailed by other method calls
     * @param name  Name of the attribute, non-null string
     * @param value Value of the attribute
     * @return Current instance
     */
    Target attribute(String name, boolean value);

    /**
     * Sets an attribute of the current instance as an array of boolean values. Call to this method can be trailed by
     * other method calls
     * @param name  Name of the attribute, non-null string
     * @param value Value of the attribute
     * @return Current instance
     */
    Target attribute(String name, boolean[] value);

    /**
     * Sets an attribute of the current instance as a long-typed number. Call to this method can be trailed by other
     * method calls
     * @param name  Name of the attribute, non-null string
     * @param value Value of the attribute
     * @return Current instance
     */
    Target attribute(String name, long value);

    /**
     * Sets an attribute of the current instance as an array of long-typed numbers. Call to this method can be trailed
     * by other method calls
     * @param name  Name of the attribute, non-null string
     * @param value Value of the attribute
     * @return Current instance
     */
    Target attribute(String name, long[] value);

    /**
     * Sets an attribute of the current instance as a double-typed number. Call to this method can be trailed by other
     * method calls
     * @param name  Name of the attribute, non-null string
     * @param value Value of the attribute
     * @return Current instance
     */
    Target attribute(String name, double value);

    /**
     * Sets an attribute of the current instance as an array of double-typed numbers. Call to this method can be trailed
     * by other method calls
     * @param name  Name of the attribute, non-null string
     * @param value Value of the attribute
     * @return Current instance
     */
    Target attribute(String name, double[] value);

    /**
     * Sets an attribute of the current instance as a {@code Date} object. Call to this method can be trailed by other
     * method calls
     * @param name  Name of the attribute, non-null string
     * @param value Value of the attribute
     * @return Current instance
     */
    Target attribute(String name, Date value);

    /**
     * Sets an attribute of the current instance as an array of {@code Date} objects. Call to this method can be trailed
     * by other method calls
     * @param name  Name of the attribute, non-null string
     * @param value Value of the attribute
     * @return Current instance
     */
    Target attribute(String name, Date[] value);

    /**
     * Assigns attributes to the current instance based on the provided {@code Map}. Call to this method can be trailed
     * by other method calls
     * @param value {@code Map} containing of name-value pairs
     * @return Current instance
     */
    Target attributes(Map<String, Object> value);

    /**
     * Assigns attributes to the current instance based on the provided {@code Annotation} object. Properties of the
     * annotation become the attribute names and values. Call to this method can be trailed by other method calls
     * @param value {@code Annotation} object used as the source of data
     * @return Current instance
     */
    default Target attributes(Annotation value) {
        return attributes(value, null);
    }

    /**
     * Assigns attributes to the current instance based on the provided {@code Annotation} object. Properties of the
     * annotation become the attribute names and values. Call to this method can be trailed by other method calls
     * @param value  {@code Annotation} object used as the source of data
     * @param filter {@code Predicate} used to sort out irrelevant properties
     * @return Current instance
     */
    Target attributes(Annotation value, Predicate<Method> filter);

    /**
     * Removes an attribute of the current instance specified by name
     * @param name Name of the attribute, non-null string
     */
    void removeAttribute(String name);

    /**
     * Adapts the current {@code Target} instance to the provided type
     * @param adaptation {@code Class} reference indicating the desired data type
     * @param <T>        The type of the resulting value
     * @return The {@code T}-typed object or null in case the adaptation to the particular type was not possible or failed
     */
    <T> T adaptTo(Class<T> adaptation);
}
