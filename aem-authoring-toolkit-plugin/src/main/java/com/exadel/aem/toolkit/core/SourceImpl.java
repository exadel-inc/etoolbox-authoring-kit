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

package com.exadel.aem.toolkit.core;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.core.util.DialogConstants;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SourceImpl implements Source {

    private final Member member;
    private final Map<String, Object> valueMap;
    private Class<?> processedClass;

    public SourceImpl(Member member) {
        this.member = member;
        this.valueMap = new HashMap<>();
        this.valueMap.put(DialogConstants.PN_PREFIX, "./");
        this.valueMap.put(DialogConstants.PN_POSTFIX, "");
    }

    @Override
    public Object fromValueMap(String key) {
        return valueMap.get(key);
    }

    @Override
    public Object addToValueMap(String key, String value) {
        return valueMap.put(key, value);
    }

    @Override
    public <T> T adaptTo(Class<T> target) {
        if (target == null || member == null) {
            return null;
        }
        if (target.isAnnotation()) {
            return target.cast(getDeclaredAnnotation((Class<? extends Annotation>) target));
        }
        if (target.isArray()) {
            if (target.getComponentType().equals(Annotation.class)) {
                return target.cast(getDeclaredAnnotations());
            } else {
                return target.cast(getAnnotationsByType((Class<? extends Annotation>) target.getComponentType()));
            }
        }
        T result;
        try {
            result = target.getConstructor().newInstance();
            for (Field resultField : target.getFields()) {
                resultField.setAccessible(true);
                resultField.set(result, findValue(resultField.getName()));
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return null;
        }
        return result;
    }

    @Override
    public Member getSource() {
        return member;
    }

    @Override
    public Class<?> getProcessedClass() {
        return this.processedClass;
    }

    @Override
    public void setProcessedClass(Class<?> processedClass) {
        this.processedClass = processedClass;
    }

    private Object findValue(String name) {
        for (Annotation annotation : getDeclaredAnnotations()) {
            try {
                Method annotationMethod = annotation.annotationType().getMethod(name);
                return annotationMethod.invoke(annotation, new Object[0]);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                continue;
            }
        }
        return null;
    }

    private Annotation[] getDeclaredAnnotations() {
        return  member instanceof Field
                ? ((Field) member).getDeclaredAnnotations()
                : ((Method) member).getDeclaredAnnotations();
    }

    private <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return  member instanceof Field
                ? ((Field) member).getDeclaredAnnotation(annotationClass)
                : ((Method) member).getDeclaredAnnotation(annotationClass);
    }

    private <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return  member instanceof Field
                ? ((Field) member).getAnnotationsByType(annotationClass)
                : ((Method) member).getAnnotationsByType(annotationClass);
    }
}
