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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.api.annotations.editconfig.ChildEditConfig;
import com.exadel.aem.toolkit.api.annotations.editconfig.EditConfig;
import com.exadel.aem.toolkit.api.annotations.widgets.accessory.HtmlTag;

public class TestXmlWriterHelper {
    private static final Logger LOG = LoggerFactory.getLogger(TestXmlWriterHelper.class);

    private TestXmlWriterHelper() {
    }

    public static boolean doTest(String testedClass, Path pathToExpectedFiles) throws ClassNotFoundException {
        Class dialogClass = Class.forName(testedClass);
        List<PackageEntryWriter> writers = getWriters(dialogClass);

        Map<String, String> actualFiles = getActualFiles(dialogClass, writers);
        if (pathToExpectedFiles == null) {
            return true;
        }
        Map<String, String> expectedFiles = getExpectedFiles(pathToExpectedFiles);
        return compare(actualFiles, expectedFiles, pathToExpectedFiles.toString());
    }

    private static List<PackageEntryWriter> getWriters(Class dialogClass) {
        List<PackageEntryWriter> writers = new ArrayList<>();
        try {
            DocumentBuilder documentBuilder = PackageWriter.createDocumentBuilder();
            Transformer transformer = PackageWriter.createTransformer();
            writers.add(new ContentXmlWriter(documentBuilder, transformer));
            writers.add(new CqDialogWriter(documentBuilder, transformer));
            if (dialogClass.isAnnotationPresent(EditConfig.class)) {
                writers.add(new CqEditConfigWriter(documentBuilder, transformer));
            }
            if (dialogClass.isAnnotationPresent(ChildEditConfig.class)) {
                writers.add(new CqChildEditConfigWriter(documentBuilder, transformer));
            }
            if (dialogClass.isAnnotationPresent(HtmlTag.class)) {
                writers.add(new CqHtmlTagWriter(documentBuilder, transformer));
            }
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            LOG.error(e.getMessage());
        }

        return writers;
    }

    private static Map<String, String> getActualFiles(Class dialogClass, List<PackageEntryWriter> writers) {
        Map<String, String> actualFiles = new HashMap<>();
        writers.forEach(packageEntryWriter -> {
            try (StringWriter stringWriter = new StringWriter()){
                packageEntryWriter.writeXml(dialogClass, stringWriter);
                actualFiles.put(packageEntryWriter.getXmlScope().toString(), stringWriter.toString());
            } catch (IOException ex) {
                LOG.error("Could not implement test writer", ex);
            }
        });
        return actualFiles;
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
