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

package com.exadel.aem.toolkit.core.maven;

import com.exadel.aem.toolkit.core.util.writer.TestXmlWriterHelper;

class ExceptionsTestBase extends DefaultTestBase {
    private static final String EXCEPTION_SETTING = "all";

    @Override
    String getExceptionSetting() {
        return EXCEPTION_SETTING;
    }

    @Override
    void test(Class<?> tested) {
        try {
            TestXmlWriterHelper.doTest(tested.getName(), null);
        } catch (ClassNotFoundException ex) {
            LOG.error("Cannot initialize instance of class " + tested.getName(), ex);
        }
    }
}
