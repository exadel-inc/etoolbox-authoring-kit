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

import java.nio.file.Paths;

public class TestConstants {

    public static final String CONTENT_ROOT_PATH = "src/test/resources";

    public static final String RESOURCE_FOLDER_COMMON = "common";
    public static final String RESOURCE_FOLDER_COMPONENT = "component";
    public static final String RESOURCE_FOLDER_DEPENDSON = "dependsOn";
    public static final String RESOURCE_FOLDER_WIDGET = "widget";

    public static final String PLUGIN_MODULE_TARGET = Paths.get("target", "classes").toAbsolutePath().toString();
    public static final String PLUGIN_MODULE_TEST_TARGET = Paths.get( "target", "test-classes").toAbsolutePath().toString();

    private static final String API_MODULE_NAME = "etoolbox-authoring-kit-core";
    private static final String PLUGIN_MODULE_NAME = "etoolbox-authoring-kit-plugin";
    public static final String API_MODULE_TARGET = PLUGIN_MODULE_TARGET.replace(PLUGIN_MODULE_NAME, API_MODULE_NAME);

    public static final String DEFAULT_COMPONENT_NAME = "test-component";
    public static final String DEFAULT_COMPONENT_TITLE = "Test Component";
    public static final String DEFAULT_COMPONENT_DESCRIPTION = "Test component description";
    public static final String DEFAULT_COMPONENT_GROUP = "Test Component Group";
    public static final String DEFAULT_COMPONENT_SUPERTYPE = "test/component/supertype";

    public static final String LABEL_TAB_0 = "Zeroth tab";
    public static final String LABEL_TAB_1 = "First tab";
    public static final String LABEL_TAB_2 = "Second tab";
    public static final String LABEL_TAB_3 = "Third tab";
    public static final String LABEL_TAB_4 = "Fourth tab";
    public static final String LABEL_TAB_5 = "Fifth tab";
    public static final String LABEL_TAB_6 = "Sixth tab";
    public static final String LABEL_TAB_7 = "Seventh tab";

    private TestConstants() {
    }
}
