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
package com.exadel.aem.toolkit.plugin.utils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.jimfs.Jimfs;

public class FileSystemHelper implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(FileSystemHelper.class);

    private static final String EXCEPTION_MESSAGE = "Could not property initialize testing file system";

    private FileSystem fileSystem;

    public FileSystem getFileSystem() {
        if (fileSystem == null) {
            fileSystem = createFileSystem();
        }
        return fileSystem;
    }

    @Override
    public void close() throws IOException {
        if (fileSystem != null) {
            fileSystem.close();
        }
    }

    private static FileSystem createFileSystem() {
        FileSystem fileSystem = Jimfs.newFileSystem();
        Path componentPath = fileSystem.getPath(TestConstants.DEFAULT_COMPONENT_NAME);
        try {
            Files.createDirectory(componentPath);
        } catch (IOException e) {
            LOG.error(EXCEPTION_MESSAGE, e);
        }
        return fileSystem;
    }
}
