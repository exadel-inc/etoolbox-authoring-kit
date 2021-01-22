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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.api.annotations.main.Component;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;
import com.exadel.aem.toolkit.plugin.exceptions.PluginException;
import com.exadel.aem.toolkit.plugin.util.XmlDocumentFactory;

public class TestXmlWriterHelper {
    private static final Logger LOG = LoggerFactory.getLogger(TestXmlWriterHelper.class);

    private TestXmlWriterHelper() {
    }

    public static boolean doTest(String testedClass, Path pathToExpectedFiles) throws ClassNotFoundException {
        Class<?> dialogClass = Class.forName(testedClass);
        List<PackageEntryWriter> writers = getWriters();

        Map<String, String> actualFiles = getActualFiles(dialogClass, writers);
        if (pathToExpectedFiles == null) {
            return true;
        }
        Map<String, String> expectedFiles = getExpectedFiles(pathToExpectedFiles);
        return compare(actualFiles, expectedFiles, pathToExpectedFiles.toString());
    }

    private static List<PackageEntryWriter> getWriters() {
        List<PackageEntryWriter> writers = new ArrayList<>();
        try {
            Transformer transformer = XmlDocumentFactory.newDocumentTransformer();
            writers.add(new ContentXmlWriter(transformer));
            writers.add(new CqDialogWriter(transformer, XmlScope.CQ_DIALOG));
            writers.add(new CqDialogWriter(transformer, XmlScope.CQ_DESIGN_DIALOG));
            writers.add(new CqEditConfigWriter(transformer));
            writers.add(new CqChildEditConfigWriter(transformer));
            writers.add(new CqHtmlTagWriter(transformer));
        } catch (TransformerConfigurationException e) {
            LOG.error(e.getMessage());
        }

        return writers;
    }

    private static Map<String, String> getActualFiles(Class<?> dialogClass, List<PackageEntryWriter> writers) {
        Map<String, String> actualFiles = new HashMap<>();
        List<Class<?>> views = new LinkedList<>(Collections.singletonList(dialogClass));
        Optional.ofNullable(dialogClass.getAnnotation(Component.class)).ifPresent(component -> Collections.addAll(views, component.views()));
        writers.forEach(packageEntryWriter -> {
            try (StringWriter stringWriter = new StringWriter()) {
                if (packageEntryWriter instanceof ContentXmlWriter) {
                    writeContent(packageEntryWriter, stringWriter, views);
                    actualFiles.put(packageEntryWriter.getXmlScope().toString(), stringWriter.toString());
                    return;
                }
                List<Class<?>> processedClasses = views.stream().filter(packageEntryWriter::canProcess).collect(Collectors.toList());
                if (processedClasses.size() > 1) {
                    throw new IOException();
                }
                if (processedClasses.isEmpty()) {
                    return;
                }
                packageEntryWriter.writeXml(processedClasses.get(0), stringWriter);
                actualFiles.put(packageEntryWriter.getXmlScope().toString(), stringWriter.toString());
            } catch (PluginException pe) {
                // Deliberately re-throwing to distinguish from an unanticipated exception (needed for exceptions testcases)
                throw pe;
            } catch (Exception e) {
                LOG.error("Could not implement test writer", e);
            }
        });
        return actualFiles;
    }

    private static void writeContent(PackageEntryWriter writer, StringWriter stringWriter, List<Class<?>> views) throws Exception {
        List<Class<?>> processedClasses = views.stream().filter(writer::canProcess).collect(Collectors.toList());
        if (processedClasses.size() > 2) {
            throw new IOException();
        }
        if (processedClasses.size() == 2) {
            if (processedClasses.get(0).getDeclaredAnnotation(Component.class) != null) {
                writer.writeXml(processedClasses.get(0), stringWriter);
            } else {
                writer.writeXml(processedClasses.get(1), stringWriter);
            }
            return;
        }
        if (!processedClasses.isEmpty()) {
            writer.writeXml(processedClasses.get(0), stringWriter);
        }
    }

    private static Map<String, String> getExpectedFiles(Path componentsPath) {
        Map<String, String> expectedFiles = new HashMap<>();
        try {
            for (File file : Objects.requireNonNull(componentsPath.toFile().listFiles())) {
                expectedFiles.put(file.getName(), String.join("", Files.readAllLines(componentsPath.resolve(file.getName()))));
            }
        } catch (NullPointerException | IOException ex) {
            LOG.error("Could not read the package " + componentsPath, ex);
        }
        return expectedFiles;
    }

    private static boolean compare(Map<String, String> actualFiles, Map<String, String> expectedFiles, String resourcePath) {
        if (!mapsCorrespond(actualFiles, expectedFiles)) {
            return false;
        }
        return expectedFiles.entrySet().stream()
                .allMatch(entry -> {
                    try {
                        return FilesComparator.compareXMLFiles(actualFiles.get(entry.getKey()),
                                entry.getValue(),
                                resourcePath + File.separator + entry.getKey());
                    } catch (Exception ex) {
                        LOG.error("Could not implement XML files comparison", ex);
                    }
                    return false;
                });
    }

    private static boolean mapsCorrespond(Map<String, String> first, Map<String, String> second) {
        if (first == null || second == null || first.size() != second.size()) {
            return false;
        }
        return first.keySet().stream().allMatch(second::containsKey);
    }
}
