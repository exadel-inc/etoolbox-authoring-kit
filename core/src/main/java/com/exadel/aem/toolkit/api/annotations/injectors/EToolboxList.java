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
package com.exadel.aem.toolkit.api.annotations.injectors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Source;
import org.apache.sling.models.spi.injectorspecific.InjectAnnotation;

import com.exadel.aem.toolkit.core.injectors.EToolboxListInjector;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItem;
import com.exadel.aem.toolkit.core.lists.utils.ListHelper;

/**
 * Used on a field, a method, or a method parameter of a Sling model to inject items of an EToolbox List.
 * <p>Injects values of the same types that {@link ListHelper} can produce. An array/collection of items can consist of
 * {@link Resource}s, {@link SimpleListItem}s, or else arbitrary list items as soon as they are backed by a
 * resource-adapted (not request-adapted) Sling model.
 * <p>If the annotated member is of type {@code Collection}, {@code List}, {@code Map}, or else an
 * array of items, the collection of list entries is injected. Otherwise, nothing is injected.</p>
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@InjectAnnotation
@Source(EToolboxListInjector.NAME)
public @interface EToolboxList {

    /**
     * Specifies the path to a list of items
     * @return Required non-blank string
     */
    String value();

    /**
     * Specifies the key attribute in a resource that identifies an item in the list. This setting is applicable only to
     * Java class members of the {@code Map<String, T>} type
     * @return Optional non-blank string
     */
    String keyProperty() default "";
}
