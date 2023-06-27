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
package com.exadel.aem.toolkit.plugin.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.stream.Stream;

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

    static <T> T from(Class<T> type) {
        return from(type, Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    static <T> T from(Class<T> type, Map<String, Object> properties) {
        Object result = Proxy.newProxyInstance(type.getClassLoader(),
            new Class[]{type, Metadata.class},
            new InterfaceHandler<>(type, properties));
        return (T) result;
    }

    static <T extends Annotation> T from(T source, Class<T> type) {
        return type.cast(from(source));
    }

    static <T extends Annotation> Metadata from(T source) {
        return from(source, (Map<String, Object>) null);
    }

    static <T extends Annotation> Metadata from(T source, Map<String, Object> properties) {
        if (source instanceof Metadata && properties == null) {
            return (Metadata) source;
        } else if (source instanceof Metadata) {
            properties.forEach((key, value) -> ((Metadata) source).putValue(key, value));
            return (Metadata) source;
        }
        Object newInstance = Proxy.newProxyInstance(Metadata.class.getClassLoader(),
            new Class[]{source.annotationType(), Metadata.class},
            new InterfaceHandler<>(source, properties));
        return (Metadata) newInstance;
    }
}
