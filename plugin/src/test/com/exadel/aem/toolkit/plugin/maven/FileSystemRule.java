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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.jimfs.Jimfs;

public class FileSystemRule implements TestRule {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemRule.class);

    private static final String EXCEPTION_INIT = "Could not initialize testing file system";
    private static final String EXCEPTION_CLOSE = "Could not close testing file system";

    private FileSystem fileSystem;

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                fileSystem = prepareFileSystem();
                try {
                    base.evaluate();
                } finally {
                    tearDown();
                }
            }
        };
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    private void tearDown() {
        if (fileSystem == null) {
            return;
        }
        try {
            fileSystem.close();
        } catch (IOException e) {
            LOG.error(EXCEPTION_CLOSE, e);
        }
    }

    private static FileSystem prepareFileSystem() {
        FileSystem fileSystem = Jimfs.newFileSystem();
        Path componentPath = fileSystem.getPath(TestConstants.PACKAGE_ROOT_PATH + TestConstants.DEFAULT_COMPONENT_NAME);
        try {
            Files.createDirectories(componentPath);
        } catch (IOException e) {
            LOG.error(EXCEPTION_INIT, e);
        }
        return fileSystem;
    }
}
