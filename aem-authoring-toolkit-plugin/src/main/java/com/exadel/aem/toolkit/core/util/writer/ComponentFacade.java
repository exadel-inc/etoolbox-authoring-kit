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

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.annotations.main.Component;
import com.exadel.aem.toolkit.core.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;

/**
 * Class that encapsulates steps taken by MVC pattern
 */
public class ComponentFacade {

    private static final String EXCEPTION_MESSAGE_TEMPLATE = "Can't choose view for %s";

    private final List<PackageEntryWriter> writers;
    private final List<Class<?>> views;

    public ComponentFacade(List<PackageEntryWriter> writers, Class<?> componentClass) {
        this.writers = writers;
        this.views = new LinkedList<>(Collections.singletonList(componentClass));
        Optional.ofNullable(componentClass.getAnnotation(Component.class))
            .ifPresent(component -> Collections.addAll(views, component.views()));
    }

    /**
     * Method that define each view for all scopes
     * @param path Current {@code Path} instance
     */
    public void write(Path path) {
        writers.forEach(writer -> {
            if (writer instanceof ContentXmlWriter) {
                writeContent(writer, path);
                return;
            }
            List<Class<?>> processedClasses = views.stream().filter(writer::isProcessed).collect(Collectors.toList());
            if (processedClasses.size() > 1) {
                PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(String.format(EXCEPTION_MESSAGE_TEMPLATE, writer.getXmlScope())));
                return;
            }
            if (processedClasses.isEmpty()) {
                return;
            }
            writer.writeXml(processedClasses.get(0), path);
        });
    }

    private void writeContent(PackageEntryWriter writer, Path path) {
        List<Class<?>> processedClasses = views.stream().filter(writer::isProcessed).collect(Collectors.toList());
        if (processedClasses.size() > 2) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(String.format(EXCEPTION_MESSAGE_TEMPLATE, writer.getXmlScope())));
            return;
        }
        if (processedClasses.size() == 2) {
            if (processedClasses.get(0).getDeclaredAnnotation(Component.class) != null) {
                writer.writeXml(processedClasses.get(0), path);
            } else {
                writer.writeXml(processedClasses.get(1), path);
            }
            return;
        }
        if (!processedClasses.isEmpty()) {
            writer.writeXml(processedClasses.get(0), path);
        }
    }
}
