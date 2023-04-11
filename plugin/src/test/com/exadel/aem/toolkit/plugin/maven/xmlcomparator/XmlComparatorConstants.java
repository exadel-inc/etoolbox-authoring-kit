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
package com.exadel.aem.toolkit.plugin.maven.xmlcomparator;

import org.apache.commons.lang3.StringUtils;

class XmlComparatorConstants {

    static final int LOG_INDENT_WIDTH = 1;
    static final int DEFAULT_LOG_TABLE_WIDTH = 80;
    static final String LOG_COLUMN_SEPARATOR = " | ";

    static final String LOG_INDENT = StringUtils.repeat(StringUtils.SPACE, LOG_INDENT_WIDTH);
    static final String SEPARATOR_ATTRIBUTE = "/@";

    private XmlComparatorConstants() {
    }
}
