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

package com.exadel.aem.toolkit.core.util.writer;

import com.exadel.aem.toolkit.api.annotations.main.Component;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ComponentFacade {
    private final List<PackageEntryWriter> writers;
    private final List<Class<?>> views;

    public ComponentFacade(List<PackageEntryWriter> writers, Class<?> componentClass) {
        this.writers = writers;
        this.views = new LinkedList<>(Collections.singletonList(componentClass));
        Optional.ofNullable(componentClass.getAnnotation(Component.class))
                .ifPresent(component -> Collections.addAll(views, component.views()));
    }

    public void write(Path path) {
        writers.forEach(writer -> {
            Class<?> processedClass = views.stream().filter(writer::isProcessed).findFirst().orElse(null);
            if (processedClass == null) {
                return;
            }
            writer.writeXml(processedClass, path);
        });
    }
}
