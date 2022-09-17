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

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.exadel.aem.toolkit.plugin.accessories.FileSystemHost;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;
import com.exadel.aem.toolkit.plugin.utils.TestConstants;
import com.exadel.aem.toolkit.plugin.writers.TestXmlUtility;

public abstract class ExceptionsTestBase {

    private static FileSystemHost fileSystemHelper;

    @BeforeClass
    public static void setUp() {
        fileSystemHelper = new FileSystemHost();
        PluginSettings settings = PluginSettings
            .builder()
            .componentsPathBase(TestConstants.PACKAGE_ROOT_PATH)
            .terminateOn(DialogConstants.VALUE_ALL)
            .build();
        PluginRuntime.contextBuilder()
            .classPathElements(DefaultTestBase.CLASSPATH_ELEMENTS)
            .settings(settings)
            .build();
    }

    @AfterClass
    public static void finalizeAll() throws IOException {
        fileSystemHelper.close();
    }

    void test(Class<?> tested) {
        try {
            TestXmlUtility.doTest(fileSystemHelper.getFileSystem(), tested.getName(), null);
        } catch (ClassNotFoundException cnfe) {
            DefaultTestBase.LOG.error(DefaultTestBase.INSTANTIATION_EXCEPTION_MESSAGE + tested.getName(), cnfe);
        } catch (IOException ioe) {
            DefaultTestBase.LOG.error(DefaultTestBase.CLEANUP_EXCEPTION_MESSAGE + tested.getName(), ioe);
        }
    }
}
