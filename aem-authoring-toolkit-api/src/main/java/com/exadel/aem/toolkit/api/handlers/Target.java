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

import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;

public interface Target {

    String getName();

    Target getParent();

    List<Target> getChildren();

    default boolean exists(String path) {
        return getTarget(path) != null;
    }

    XmlScope getScope();

    Target getTarget(String path);

    Target getOrCreateTarget(String path);

    Target createTarget(String path);

    void removeTarget(String path);

    String getNamePrefix();

    Target namePrefix(String prefix);

    String getNamePostfix();

    Target namePostfix(String postfix);

    Map<String, String> getAttributes();

    Target attribute(String name, String value);

    Target attribute(String name, boolean value);

    Target attribute(String name, long value);

    Target attribute(String name, double value);

    Target attribute(String name, Date value);

    Target attributes(Map<String, Object> map);

    Target attributes(Annotation annotation, Predicate<Method> filter);

    <T> T adaptTo(Class<T> adaptation);
}
