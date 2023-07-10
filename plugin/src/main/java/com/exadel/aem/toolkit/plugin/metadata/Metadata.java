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
package com.exadel.aem.toolkit.plugin.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.stream.Stream;

import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

public interface Metadata extends Annotation, Iterable<Property> {

    <T extends Annotation> T getAnnotation(Class<T> type);

    Annotation getAnyAnnotation(Class<?>... types);

    boolean hasProperty(String path);

    boolean hasProperty(PropertyPath path);

    Property getProperty(String path);

    Property getProperty(PropertyPath path);

    Object getValue(String path);

    Object getValue(PropertyPath path);

    Iterator<Property> iterator(boolean deepRead, boolean expandArrays);

    Spliterator<Property> spliterator(boolean deepRead, boolean expandArrays);

    Stream<Property> stream();

    Stream<Property> stream(boolean deepRead, boolean expandArrays);

    Object putValue(PropertyPath path, Object value);

    Object putValue(String path, Object value);

    Object unsetValue(String path);

    static <T extends Annotation> T from(Class<T> type) {
        return from(type, Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    static <T extends Annotation> T from(Class<T> type, Map<String, Object> properties) {
        return (T) from(null, type, properties);
    }

    static <T extends Annotation> Metadata from(T source) {
        return from(source, null, null);
    }

    static <T extends Annotation> Metadata from(T source, Map<String, Object> properties) {
        return from(source, null, properties);
    }

    static <T extends Annotation> Metadata from(T source, Class<T> type, Map<String, Object> properties) {
        if (source instanceof Metadata) {
            if (properties != null && !properties.isEmpty()) {
                properties.forEach((key, value) -> ((Metadata) source).putValue(key, value));
            }
            return (Metadata) source;
        }
        Class<? extends Annotation> effectiveType = source != null ? source.annotationType() : type;
        InterfaceHandler<T> interfaceHandler = source != null
            ? new InterfaceHandler<>(source, properties)
            : new InterfaceHandler<>(type, properties);
        Object newInstance = Proxy.newProxyInstance(
            PluginRuntime.context().getReflection().getClassLoader(),
            new Class[]{effectiveType, Metadata.class},
            interfaceHandler);
        return (Metadata) newInstance;
    }
}
