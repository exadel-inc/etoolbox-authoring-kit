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

package com.exadel.aem.toolkit.plugin.source;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.widgets.FieldSet;
import com.exadel.aem.toolkit.api.annotations.widgets.MultiField;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.markers._Default;
import com.exadel.aem.toolkit.plugin.exceptions.ReflectionException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

public abstract class SourceBase implements Source {

    private static final String CACHE_KEY_TEMPLATE = "%s#%s";
    private static final String HOLDER_EXCEPTION_MESSAGE = "Settings holder class missing";

    private final Class<?> reportingClass;

    private Map<String, Object> settingsCache;

    SourceBase(Class<?> reportingClass) {
        this.reportingClass = reportingClass;
    }

    @Override
    public Class<?> getReportingClass() {
        return this.reportingClass;
    }

    @Override
    public Class<?> getValueType() {
        // Retrieve the "immediate" return type
        Class<?> result = getPlainReturnType();
        // Then switch to directly specified type, if any
        if (getDeclaredAnnotation(MultiField.class) != null
            && getDeclaredAnnotation(MultiField.class).field() != _Default.class) {
            result = getDeclaredAnnotation(MultiField.class).field();
        } else if (getDeclaredAnnotation(FieldSet.class) != null
            && getDeclaredAnnotation(FieldSet.class).source() != _Default.class) {
            result = getDeclaredAnnotation(FieldSet.class).source();
        }
        return result;
    }

    abstract Class<?> getPlainReturnType();

    abstract Annotation[] getDeclaredAnnotations();

    abstract <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass);

    abstract <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass);

    @Override
    public <T> T getSetting(Class<? extends Annotation> holder, String name, T defaultValue) {
        if (holder == null || StringUtils.isBlank(name)) {
            return defaultValue;
        }
        if (settingsCache != null) {
            String key = String.format(CACHE_KEY_TEMPLATE, holder.getName(), name);
            if (settingsCache.containsKey(key)) {
                @SuppressWarnings("unchecked")
                T result = (T) settingsCache.getOrDefault(key, defaultValue);
                return result;
            }
        }
        Annotation adapted = adaptTo(holder);
        if (adapted == null) {
            return defaultValue;
        }
        try {
            @SuppressWarnings("unchecked")
            T result = (T) adapted.annotationType().getDeclaredMethod(name).invoke(adapted);
            return result;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassCastException e) {
            return defaultValue;
        }
    }

    @Override
    public void storeSetting(Class<? extends Annotation> holder, String name, Object value) {
        try {
            Objects.requireNonNull(holder).getMethod(StringUtils.defaultString(name)); // this is to check if method provided actually exists
        } catch (NoSuchMethodException | NullPointerException e) {
            ReflectionException re = holder != null
                ? new ReflectionException(holder, name)
                : new ReflectionException(HOLDER_EXCEPTION_MESSAGE, e);
            PluginRuntime.context().getExceptionHandler().handle(re);
            return;
        }
        if (settingsCache == null) {
            settingsCache = new HashMap<>();
        }
        settingsCache.put(String.format(CACHE_KEY_TEMPLATE, holder.getName(), name), value);
    }

    @Override
    public <T> T adaptTo(Class<T> adaptation) {
        if (adaptation == null) {
            return null;
        }
        if (adaptation.isAnnotation()) {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) adaptation;
            return adaptation.cast(getDeclaredAnnotation(annotationClass));
        }
        if (adaptation.isArray()) {
            if (adaptation.getComponentType().equals(Annotation.class)) {
                return adaptation.cast(getDeclaredAnnotations());
            } else {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) adaptation.getComponentType();
                return adaptation.cast(getAnnotationsByType(annotationClass));
            }
        }
        return null;
    }

    public static Source fromMember(Member member, Class<?> processedClass) {
        return member instanceof Field
            ? new SourceFieldImpl((Field) member, processedClass)
            : new SourceMethodImpl((Method) member, processedClass);
    }
}
