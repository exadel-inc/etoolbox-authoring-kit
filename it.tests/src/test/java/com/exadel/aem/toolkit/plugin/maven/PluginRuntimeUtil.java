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

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class PluginRuntimeUtil {

    private static final String CURRENT_MODULE_TARGET = Paths.get("target", "classes").toAbsolutePath().toString();
    private static final String CURRENT_MODULE_TEST_TARGET = Paths.get( "target", "test-classes").toAbsolutePath().toString();
    private static final String API_MODULE_TARGET = CURRENT_MODULE_TARGET.replace("etoolbox-authoring-kit-plugin", "etoolbox-authoring-kit-core");

    private static final List<String> CLASSPATH_ELEMENTS = Arrays.asList(
        CURRENT_MODULE_TARGET,
        CURRENT_MODULE_TEST_TARGET,
        API_MODULE_TARGET
    );


    private PluginRuntimeUtil() {
    }

    public static void doInit() {
        PluginRuntime
            .contextBuilder()
            .settings(PluginSettings.EMPTY)
            .classPathElements(CLASSPATH_ELEMENTS)
            .build();
    }

    public static void doClose() {
        PluginRuntime.close();
    }
}
