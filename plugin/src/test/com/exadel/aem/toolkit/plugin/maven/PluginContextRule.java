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

import java.util.Arrays;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class PluginContextRule implements TestRule {

    private static final List<String> CLASSPATH_ELEMENTS = Arrays.asList(
        TestConstants.PLUGIN_MODULE_TARGET,
        TestConstants.API_MODULE_TARGET,
        TestConstants.PLUGIN_MODULE_TEST_TARGET
    );

    private static MuteableExceptionHandler exceptionHandler;

    @Override
    public Statement apply(Statement statement, Description description) {
        if (!isInitialized()) {
            initialize();
        }
        if (description.getAnnotation(ThrowsPluginException.class) == null
            && description.getTestClass().getAnnotation(ThrowsPluginException.class) == null) {
            return statement;
        }
        exceptionHandler.unmute();
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    statement.evaluate();
                } finally {
                    exceptionHandler.mute();
                }
            }
        };
    }

    private static boolean isInitialized() {
        return exceptionHandler != null;
    }

    private void initialize() {
        PluginSettings settings = PluginSettings
            .builder()
            .componentsPathBase(TestConstants.PACKAGE_ROOT_PATH)
            .build();
        exceptionHandler = new MuteableExceptionHandler();
        PluginRuntime.contextBuilder()
            .classPathElements(CLASSPATH_ELEMENTS)
            .settings(settings)
            .exceptionHandler(exceptionHandler)
            .build();
    }
}
