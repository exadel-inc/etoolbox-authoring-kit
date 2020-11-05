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
package com.exadel.aem.toolkit.core.handlers.widget.common;

import java.lang.reflect.Member;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.SourceImpl;

import com.exadel.aem.toolkit.api.annotations.widgets.Extends;
import com.exadel.aem.toolkit.core.handlers.widget.DialogWidget;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.handlers.widget.DialogWidgets;

/**
 * Handler for processing Granite UI widgets features "inherited" by the current component class {@code SourceFacade} from
 * other SourceFacades via {@link Extends} mechanism
 */
public class InheritanceHandler implements BiConsumer<Source, Target> {
    private BiConsumer<Source, Target> descendantChain;
    public InheritanceHandler(BiConsumer<Source, Target> descendantChain) {
        this.descendantChain = descendantChain;
    }

    /**
     * Processes the user-defined data and writes it to XML entity
     * @param source Current {@code SourceFacade} instance
     * @param target Current {@code TargetFacade} instance
     */
    @Override
    public void accept(Source source, Target target) {
        if (descendantChain == null) return;
        Deque<Source> inheritanceTree = getInheritanceTree(source);
        while (!inheritanceTree.isEmpty()) {
            descendantChain.accept(inheritanceTree.pollLast(), target); // to render 'ancestors' of context source starting from next handler in chain
        }
    }

    /**
     * Builds the inheritance sequence for the current {@code Field}
     * @param source Current {@code SourceFacade} instance
     * @return Ancestral {@code SourceFacade}s, as an ordered sequence
     */
    private static Deque<Source> getInheritanceTree(Source source) {
        Deque<Source> result = new LinkedList<>();
        DialogWidget referencedComponent = DialogWidgets.fromSourceFacade(source);
        if (referencedComponent == null) {
            return result;
        }
        Extends extendsAnnotation = source.adaptTo(Extends.class);
        while (extendsAnnotation != null) {
            String referencedFieldName = extendsAnnotation.field().isEmpty() ? ((Member) source.getSource()).getName() : extendsAnnotation.field();
            try {
                Source referencedField = new SourceImpl(extendsAnnotation.value().getDeclaredField(referencedFieldName));
                if (referencedField.equals(source) || result.contains(referencedField)) { // to avoid circular references
                    break;
                }
                if (referencedComponent.equals(DialogWidgets.fromSourceFacade(referencedField))) { // to avoid mixing up props of different components
                    result.add(referencedField);
                }
                extendsAnnotation = referencedField.adaptTo(Extends.class);
            } catch (NoSuchFieldException e) {
                PluginRuntime.context().getExceptionHandler().handle(e);
                extendsAnnotation = null;
            }
        }
        return result;
    }
}
