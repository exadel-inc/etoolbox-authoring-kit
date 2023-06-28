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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class PluginContextRule implements TestRule {

    private static final String PLUGIN_MODULE_TARGET = Paths.get("target", "classes").toAbsolutePath().toString();
    private static final String PLUGIN_MODULE_TEST_TARGET = Paths.get( "target", "test-classes").toAbsolutePath().toString();
    private static final String API_MODULE_TARGET = PLUGIN_MODULE_TARGET.replace("etoolbox-authoring-kit-plugin", "etoolbox-authoring-kit-core");

    private static final List<String> CLASSPATH_ELEMENTS = Arrays.asList(
        PLUGIN_MODULE_TARGET,
        PLUGIN_MODULE_TEST_TARGET,
        API_MODULE_TARGET
    );

    private static MuteableExceptionHandler exceptionHandler;

    @Override
    public Statement apply(Statement statement, Description description) {
        if (description.getAnnotation(ThrowsPluginException.class) == null
            && description.getTestClass().getAnnotation(ThrowsPluginException.class) == null) {
            return statement;
        }
        if (exceptionHandler != null) {
            exceptionHandler.unmute();
        }
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    statement.evaluate();
                } finally {
                    if (exceptionHandler != null) {
                        exceptionHandler.mute();
                    }
                }
            }
        };
    }

    public static void initializeContext() {
        PluginSettings settings = PluginSettings
            .builder()
            .defaultPathBase(TestConstants.PACKAGE_ROOT_PATH)
            .build();
        exceptionHandler = new MuteableExceptionHandler();
        PluginRuntime.contextBuilder()
            .classPathElements(CLASSPATH_ELEMENTS)
            .settings(settings)
            .exceptionHandler(exceptionHandler)
            .build();
    }

    public static void closeContext() {
        PluginRuntime.close();
    }

    protected static boolean isContextInitialized() {
        return exceptionHandler != null;
    }
}
