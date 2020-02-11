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

package com.exadel.aem.toolkit.core.util;

import java.nio.file.Paths;

public class TestsConstants {
    private TestsConstants() {
    }
    public static final String PATH_TO_EXPECTED_FILES = "src\\test\\resources\\dialog";

    private static final String API_MODULE_NAME = "aem-authoring-toolkit-api";
    private static final String PLUGIN_MODULE_NAME = "aem-authoring-toolkit-plugin";

    public static final String PLUGIN_MODULE_TARGET = Paths.get("target", "classes").toAbsolutePath().toString();
    public static final String PLUGIN_MODULE_TEST_TARGET = Paths.get( "target", "test-classes").toAbsolutePath().toString();
    public static final String API_MODULE_TARGET = PLUGIN_MODULE_TARGET.replace(PLUGIN_MODULE_NAME, API_MODULE_NAME);

    public static final String DEFAULT_COMPONENT_NAME = "test-component";
}
