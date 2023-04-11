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

class Line {
    private static final String CONSOLE_COLOR_PURPLE = "\033[0;35m";
    private static final String CONSOLE_COLOR_RESET = "\033[0m";

    private final String id;
    private String value;
    private int length;
    private boolean topIndent;
    private boolean bottomIndent;

    Line() {
        this(null);
    }

    private Line(String value) {
        this(null, value);
    }

    Line(String id, String value) {
        this.id = id;
        this.value = value;
        this.length = StringUtils.length(value);
    }

    String getId() {
        return id;
    }

    public boolean isTopIndent() {
        return topIndent;
    }

    public void setTopIndent(boolean topIndent) {
        this.topIndent = topIndent;
    }

    public boolean isBottomIndent() {
        return bottomIndent;
    }

    public void setBottomIndent(boolean bottomIndent) {
        this.bottomIndent = bottomIndent;
    }

    void append(String text) {
        value += text;
        length = StringUtils.length(value);
    }

    void highlight() {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        value = CONSOLE_COLOR_PURPLE + value + CONSOLE_COLOR_RESET;
    }

    @SuppressWarnings("SameParameterValue")
    String pad(int length) {
        if (this.length >= length) {
            return value;
        }
        return StringUtils.defaultString(value) + StringUtils.repeat(StringUtils.SPACE, length - this.length);
    }
}
