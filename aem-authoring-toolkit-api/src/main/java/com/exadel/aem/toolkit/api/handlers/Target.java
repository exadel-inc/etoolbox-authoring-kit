/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import com.exadel.aem.toolkit.api.annotations.meta.Scope;

public interface Target {

    String getName();

    Target getParent();

    List<Target> getChildren();

    boolean isEmpty();

    Scope getScope();

    default boolean exists(String path) {
        return getTarget(path) != null;
    }

    Target getTarget(String path);

    Target getOrCreateTarget(String path);

    Target createTarget(String path);

    default void addTarget(Target other) {
        if (other != null && other.getParent() != null) {
            other.getParent().getChildren().remove(other);
        }
        if (other != null) {
            getChildren().add(other);
        }
    }

    default void addTarget(Target other, int position) {
        if (other != null && other.getParent() != null) {
            other.getParent().getChildren().remove(other);
        }
        if (other != null) {
            getChildren().add(position, other);
        }
    }

    default void addTarget(Target other, Target near) {
        int position = near != null ? getChildren().indexOf(near) : -1;
        if (position > -1) {
            addTarget(other, position);
        } else {
            addTarget(other);
        }
    }

    default void addTarget(Target other, Target near, boolean placeAfter) {
        if (!placeAfter) {
            addTarget(other, near);
            return;
        }
        int position = near != null ? getChildren().indexOf(near) : -1;
        if (position > -1 && position < getChildren().size() - 1) {
            addTarget(other, position + 1);
        } else {
            addTarget(other);
        }
    }

    void removeTarget(String path);

    Target findParent(Predicate<Target> filter);

    Target findChild(Predicate<Target> filter);

    List<Target> findChildren(Predicate<Target> filter);

    String getNamePrefix();

    Target namePrefix(String prefix);

    String getNamePostfix();

    Target namePostfix(String postfix);

    Map<String, String> getAttributes();

    default String getAttribute(String name) {
        return getAttributes().get(name);
    }

    default String getAttribute(String name, String defaultValue) {
        return getAttributes().getOrDefault(name, defaultValue);
    }

    Target attribute(String name, String value);

    Target attribute(String name, String[] value);

    Target attribute(String name, boolean value);

    Target attribute(String name, boolean[] value);

    Target attribute(String name, long value);

    Target attribute(String name, long[] value);

    Target attribute(String name, double value);

    Target attribute(String name, double[] value);

    Target attribute(String name, Date value);

    Target attribute(String name, Date[] value);

    Target attributes(Map<String, Object> map);

    Target attributes(Annotation annotation, Predicate<Method> filter);

    default Target attributes(Annotation annotation) {
        return attributes(annotation, null);
    }

    void removeAttribute(String name);

    <T> T adaptTo(Class<T> adaptation);
}
