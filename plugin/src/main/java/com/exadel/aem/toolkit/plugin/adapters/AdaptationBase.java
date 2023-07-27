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
package com.exadel.aem.toolkit.plugin.adapters;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;

import com.exadel.aem.toolkit.api.handlers.Adapts;
import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.exceptions.ReflectionException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

/**
 * Presents a base for utility classes implementing {@link Adapts}
 * @param <T> Generic type of the adaptable, e.g. {@link Source} or {@link Target}
 */
public abstract class AdaptationBase<T> {

    private static final String ADAPTER_EXCEPTION_MESSAGE = "Could not create an adapter for ";

    private final Class<T> reflectedClass;
    private Map<Class<?>, Object> adaptations;

    /**
     * Initializes the instance with a class reference that manifests the generic type of the adaptable
     * @param reflectedClass {@code Class} reference, such as {@link Source} or {@link Target}
     */
    protected AdaptationBase(Class<T> reflectedClass) {
        this.reflectedClass = reflectedClass;
    }

    /**
     * Implements the basic adaptation functionality. This method is expected to be overridden by a descendant class and
     * internally called within an overriding method for the fallback result
     * @param type {@code Class} reference indicating the desired data type
     * @param <A>  The type of the resulting value
     * @return The {@code A}-typed object, or null in case the adaptation to the particular type was not possible or
     * failed
     */
    public <A> A adaptTo(Class<A> type) {
        if (ClassUtils.isAssignable(getClass(), type)) {
            return type.cast(this);
        }
        A cachedAdaptation = getAdaptation(type);
        if (cachedAdaptation != null) {
            return cachedAdaptation;
        }
        if (type.isAnnotationPresent(Adapts.class)
            && reflectedClass.equals(type.getAnnotation(Adapts.class).value())) {
            try {
                Object value = type.getConstructor(reflectedClass).newInstance(this);
                storeAdaptation(type, value);
                return type.cast(value);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                ReflectionException re = new ReflectionException(
                    ADAPTER_EXCEPTION_MESSAGE + type.getName(), e);
                PluginRuntime.context().getExceptionHandler().handle(re);
            }
        }
        return null;
    }

    /**
     * Retrieves whether the given adaptation is actual for the current instance
     * @param adaptation {@code Class} reference indicating the desired data type
     * @return {@code True} if the current instance can be cast to the given class in principle or the adaptation medium
     * has already been initialized; otherwise, {@code false}
     */
    public boolean hasAdaptation(Class<?> adaptation) {
        if (ClassUtils.isAssignable(getClass(), adaptation)) {
            return true;
        }
        return getAdaptation(adaptation) != null;
    }

    /**
     * Retrieves the previously cached adaptation of the given type
     * @return A nullable object
     * @param type {@code Class} reference indicating the required data type
     * @param <A> The type of the resulting value
     */
    private <A> A getAdaptation(Class<A> type) {
        if (adaptations != null && adaptations.containsKey(type)) {
            return type.cast(adaptations.get(type));
        }
        return null;
    }

    /**
     * Stores the provided adaptation result into the adaptations cache
     * @param type {@code Class} reference indicating the required data type
     * @param value Adaptation value
     */
    private void storeAdaptation(Class<?> type, Object value) {
        if (adaptations == null) {
            adaptations = new HashMap<>();
        }
        adaptations.put(type, value);
    }
}
