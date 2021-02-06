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

import com.exadel.aem.toolkit.api.handlers.Adaptable;
import com.exadel.aem.toolkit.plugin.exceptions.ReflectionException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

public abstract class AdaptationBase<T> {

    private static final String ADAPTER_EXCEPTION_MESSAGE = "Could not create adapter for ";

    private final Class<T> reflectedClass;
    private Map<Class<?>, Object> adaptationsCache;

    protected AdaptationBase(Class<T> reflectedClass) {
        this.reflectedClass = reflectedClass;
    }

    public <A> A adaptTo(Class<A> adaptation) {
        if (adaptationsCache != null && adaptationsCache.containsKey(adaptation)) {
            return adaptation.cast(adaptationsCache.get(adaptation));
        }
        if (adaptation.isAnnotationPresent(Adaptable.class)
            && reflectedClass.equals(adaptation.getAnnotation(Adaptable.class).value())) {
            try {
                Object result = adaptation.getConstructor(reflectedClass).newInstance(this);
                if (adaptationsCache == null) {
                    adaptationsCache = new HashMap<>();
                }
                adaptationsCache.put(adaptation, result);
                return adaptation.cast(result);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                ReflectionException re = new ReflectionException(
                    ADAPTER_EXCEPTION_MESSAGE + adaptation.getName(), e);
                PluginRuntime.context().getExceptionHandler().handle(re);
            }
        }
        return null;
    }

}
