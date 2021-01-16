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

package com.exadel.aem.toolkit.plugin.util.writer;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.exadel.aem.toolkit.api.annotations.main.Component;
import com.exadel.aem.toolkit.plugin.exceptions.InvalidSettingException;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

/**
 * Class that encapsulates steps taken by MVC pattern
 */
public class ComponentWriter {

    private static final String EXCEPTION_MESSAGE_TEMPLATE = "Can't choose view for %s";

    private final List<PackageEntryWriter> writers;
    private final List<Class<?>> views;

    public ComponentWriter(Class<?> componentClass, List<PackageEntryWriter> writers) {
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
        for (PackageEntryWriter writer : writers) {
            if (writer instanceof ContentXmlWriter) {
                writeContentXml(writer, path);
                return;
            }
            List<Class<?>> processedViews = views.stream().filter(writer::isProcessed).collect(Collectors.toList());
            if (processedViews.size() > 1) {
                PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(String.format(EXCEPTION_MESSAGE_TEMPLATE, writer.getXmlScope())));
            }
            if (processedViews.isEmpty()) {
                return;
            }
            writer.writeXml(processedViews.get(0), path);
        }
    }

    private void writeContentXml(PackageEntryWriter writer, Path path) {
        List<Class<?>> processedClasses = views.stream().filter(writer::isProcessed).collect(Collectors.toList());
        if (processedClasses.isEmpty()) {
            PluginRuntime.context().getExceptionHandler().handle(new InvalidSettingException(String.format(EXCEPTION_MESSAGE_TEMPLATE, writer.getXmlScope())));
            return;
        }
        writer.writeXml(processedClasses.get(0), path);
    }
}
