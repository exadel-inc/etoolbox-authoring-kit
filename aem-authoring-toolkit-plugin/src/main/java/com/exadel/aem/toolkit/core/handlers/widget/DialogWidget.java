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

package com.exadel.aem.toolkit.core.handlers.widget;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;

import com.exadel.aem.toolkit.core.handlers.assets.dependson.DependsOnHandler;
import com.exadel.aem.toolkit.core.handlers.widget.common.AttributesHandler;
import com.exadel.aem.toolkit.core.handlers.widget.common.CustomHandler;
import com.exadel.aem.toolkit.core.handlers.widget.common.DialogFieldHandler;
import com.exadel.aem.toolkit.core.handlers.widget.common.GenericPropertiesHandler;
import com.exadel.aem.toolkit.core.handlers.widget.common.InheritanceHandler;
import com.exadel.aem.toolkit.core.handlers.widget.common.MultipleHandler;
import com.exadel.aem.toolkit.core.handlers.widget.common.PropertyMappingHandler;

/**
 * Represents an abstraction of a built-in or a custom dialog widget that has a widget annotation attached.
 * This one is used to assemble a chain of handlers to store markup required to implement particular Granite UI
 * interface element
 */
public interface DialogWidget {
    /**
     * Gets a {@code Class} definition bound to this instance
     * @return {@code Class} object
     */
    Class<? extends Annotation> getAnnotationClass();

    /**
     * Gets a "built-in" handler routine specific this widget. Not to me mixed up with a "custom" handler that can be
     * applied to several widgets, either built-in or user-defined
     * @return {@code BiConsumer<Source, Target>} instance
     */
    BiConsumer<Source, Target> getHandler();

    /**
     * Appends Granite UI markup based on the current {@link Source} to the parent node with the specified name
     * @param source Current {@link Source}
     * @param target Parent {@link Target} instance
     * @return Populated {@link Target} by the current {@link Source}
     */
    default Target appendTo(Source source, Target target) {
        return appendTo(source, target, source.getName());
    }

    /**
     * Appends Granite UI markup based on the current {@link Source} to the parent node with the specified name
     * @param source Current {@link Source}
     * @param target Parent {@link Target} instance
     * @param name The node name to store
     * @return Populated {@link Target} by the current {@link Source}
     */
    default Target appendTo(Source source, Target target, String name) {
        Target widgetChildElement = target.child(name);
        getHandlerChain().accept(source, widgetChildElement);
        return widgetChildElement;
    }

    /**
     * Generates the chain of handlers to store widget's markup
     * @return {@code BiConsumer<Source, Target>} instance
     */
    default BiConsumer<Source, Target> getHandlerChain() {
        BiConsumer<Source, Target> mainChain = new GenericPropertiesHandler()
                .andThen(new PropertyMappingHandler())
                .andThen(new AttributesHandler())
                .andThen(new DialogFieldHandler())
                .andThen(getHandler())
                .andThen(new DependsOnHandler())
                .andThen(new CustomHandler())
                .andThen(new MultipleHandler());
        return new InheritanceHandler(mainChain).andThen(mainChain);
    }
}
