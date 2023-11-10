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
package com.exadel.aem.toolkit.plugin.maven;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.plugin.sources.ComponentSource;
import com.exadel.aem.toolkit.plugin.sources.Sources;
import com.exadel.aem.toolkit.plugin.writers.PackageWriter;
import com.exadel.etoolbox.coconut.Comparator;
import com.exadel.etoolbox.coconut.OutputType;
import com.exadel.etoolbox.coconut.diff.Diff;
import com.exadel.etoolbox.coconut.diff.DiffEntry;
import com.exadel.etoolbox.coconut.filter.Filter;

class RenderingUtil {

    private static final Logger LOG = LoggerFactory.getLogger(RenderingUtil.class);

    private RenderingUtil() {
    }

    public static boolean doTest(
        FileSystem fileSystem,
        String className,
        Path createdFilesPath,
        Path sampleFilesPath)
        throws ClassNotFoundException, IOException {

        Class<?> testableClass = Class.forName(className);
        ComponentSource testable =  Sources.fromComponentClass(testableClass);
        Path effectiveCreatedFilesPath = createdFilesPath == null
            ? fileSystem.getPath(TestConstants.PACKAGE_ROOT_PATH + TestConstants.DEFAULT_COMPONENT_NAME)
            : createdFilesPath;

        PackageWriter.forFileSystem(fileSystem, TestConstants.DEFAULT_PROJECT_NAME).write(testable);

        List<Diff> differences = Comparator
            .left(sampleFilesPath, "Expected")
            .right(effectiveCreatedFilesPath, "Actual")
            .filter(new Filter() {
                @Override
                public boolean skipLine(DiffEntry value) {
                    return value.getLeft().trim().startsWith("xmlns:") || value.getRight().trim().startsWith("xmlns:");
                }
            })
            .compare();

        boolean result = Comparator.isEqual(differences);
        if (!result) {
            reportDifferences(fileSystem, differences);
        }
        cleanUp(effectiveCreatedFilesPath);
        return result;
    }

    private static void reportDifferences(FileSystem fileSystem, List<Diff> differences) {
        StringBuilder builder = new StringBuilder();
        for (Diff difference : differences) {
            String pathSeparator = fileSystem.getSeparator();
            String testFolder = pathSeparator + "test" + pathSeparator + "resources" + pathSeparator;
            String diffLocation = difference.getLeft().contains(testFolder)
                ? StringUtils.substringAfter(difference.getLeft(), testFolder)
                : difference.getLeft();
            builder.append(String.format(
                "\n\nFound %d difference(-s) testing output for %s\n\n",
                difference.getCount(),
                diffLocation));
            builder.append(difference.toString(OutputType.CONSOLE));
        }
        LOG.warn(builder.toString());
    }

    private static void cleanUp(Path path) {
        try (Stream<Path> children = Files.list(path)) {
            List<Path> paths = children.collect(Collectors.toList());
            for (Path p : paths) {
                Files.delete(p);
            }
        } catch (IOException e) {
            LOG.error("Could not in-memory file {}", path, e);
        }
    }
}
