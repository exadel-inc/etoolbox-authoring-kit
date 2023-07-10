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
package com.exadel.aem.toolkit.plugin.metadata;

import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.core.CoreConstants;

public class PropertyPath {
    private static final Pattern ORDINAL_PATTERN = Pattern.compile("\\[(\\d+)]$");
    private static final String METHOD_PATTERN = "()";

    private final String path;
    private final Queue<PropertyPathElement> elements;

    private PropertyPath(String path, Queue<PropertyPathElement> elements) {
        this.path = path;
        this.elements = elements;
    }

    public String getPath() {
        return path;
    }

    public Queue<PropertyPathElement> getElements() {
        return elements;
    }

    public static PropertyPath parse(String path) {
        String delimiter = getDelimiter(path);
        String effectivePath = StringUtils.strip(path, CoreConstants.SEPARATOR_SLASH + CoreConstants.SEPARATOR_DOT);
        String[] pathChunks = delimiter != null ? StringUtils.split(effectivePath, delimiter) : new String[] {effectivePath};
        Queue<PropertyPathElement> elements = new LinkedList<>();
        for (String pathChunk : pathChunks) {
            String name = pathChunk;
            int ordinal = -1;
            Matcher ordinalMatcher = ORDINAL_PATTERN.matcher(name);
            if (ordinalMatcher.find()) {
                name = name.substring(0, ordinalMatcher.start());
                ordinal = Integer.parseInt(ordinalMatcher.group(1));
            }
            if (StringUtils.endsWith(name, METHOD_PATTERN)) {
                name = StringUtils.substring(name, 0, METHOD_PATTERN.length() * -1);
            }
            elements.add(new PropertyPathElement(name, ordinal));
        }
        return new PropertyPath(path, elements);
    }

    private static String getDelimiter(String path) {
        if (StringUtils.lastIndexOf(path, CoreConstants.SEPARATOR_SLASH) > 0) {
            return CoreConstants.SEPARATOR_SLASH;
        }
        if (StringUtils.contains(path, CoreConstants.SEPARATOR_DOT)) {
            return CoreConstants.SEPARATOR_DOT;
        }
        return null;
    }

}
