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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.w3c.dom.Document;
import org.xmlunit.diff.ComparisonType;

class LineUtil {

    private static final String PROPERTY_INDENT = "xmlcomparator.indent";
    private static final String PROPERTY_WIDTH = "xmlcomparator.width";

    private LineUtil() {
    }

    /* --------------------
       Parsing / formatting
       -------------------- */

    static List<Line> getLines(
        Document document,
        String xPath) {

        if (document == null || xPath == null) {
            return new ArrayList<>();
        }
        return new LineFactory(
            document.getDocumentElement(),
            xPath,
            getLogColumnWidth())
            .getLines();
    }

    /* --------
       Aligning
       -------- */

    static void align(List<Line> left, List<Line> right) {
        int cursor = 0;
        while (cursor < Math.min(left.size(), right.size())) {
            String leftId = left.get(cursor).getId();
            String rightId = right.get(cursor).getId();
            if (!StringUtils.equals(leftId, rightId)) {
                int leftIdPositionInRight = findPosition(right, leftId, cursor);
                int rightIdPositionInLeft = findPosition(left, rightId, cursor);

                if (leftIdPositionInRight > cursor) {
                    for (int i = 0; i < leftIdPositionInRight - cursor; i++) {
                        left.add(cursor, new Line());
                    }
                    cursor = leftIdPositionInRight;
                } else if (rightIdPositionInLeft > cursor) {
                    for (int i = 0; i < rightIdPositionInLeft - cursor; i++) {
                        right.add(cursor, new Line());
                    }
                    cursor = rightIdPositionInLeft;
                }
            }
            cursor++;
        }
        if (left.size() < right.size()) {
            int deficit = right.size() - left.size();
            for (int i = 0; i < deficit; i++) {
                left.add(new Line());
            }
        } else if (left.size() > right.size()) {
            int deficit = left.size() - right.size();
            for (int i = 0; i < deficit; i++) {
                right.add(new Line());
            }
        }
    }

    /* ------------
       Highlighting
       ------------ */

    static void highlight(
        ComparisonType comparisonType,
        List<Line> expectedLines,
        String expectedXPath,
        List<Line> actualLines,
        String actualXPath) {

        if (comparisonType == ComparisonType.ATTR_NAME_LOOKUP) {
            if (StringUtils.contains(expectedXPath, XmlComparatorConstants.ATTRIBUTE_SEPARATOR)) {
                highlight(expectedLines, expectedXPath);
            } else if (StringUtils.contains(actualXPath, XmlComparatorConstants.ATTRIBUTE_SEPARATOR)) {
                highlight(actualLines, actualXPath);
            }

        } else if (comparisonType == ComparisonType.ATTR_VALUE
            || comparisonType == ComparisonType.ELEMENT_TAG_NAME) {
            highlight(actualLines, actualXPath);

        } else if (comparisonType == ComparisonType.CHILD_LOOKUP) {
            if (StringUtils.isEmpty(expectedXPath)) {
                highlight(actualLines, actualXPath);
            } else if (StringUtils.isEmpty(actualXPath)) {
                highlight(expectedLines, expectedXPath);
            }
        }

    }

    private static void highlight(List<Line> lines, String id) {
        int matchedPosition = findPosition(lines, id, 0);
        if (matchedPosition < 0) {
            return;
        }
        lines.get(matchedPosition++).highlight();
        while (matchedPosition < lines.size() && lines.get(matchedPosition).getId() == null) {
            lines.get(matchedPosition++).highlight();
        }
    }

    private static int findPosition(List<Line> lines, String id, int skip) {
        if (id == null) {
            return -1;
        }
        return lines
            .stream()
            .skip(skip)
            .filter(line -> StringUtils.equals(line.getId(), id))
            .mapToInt(lines::indexOf)
            .findFirst()
            .orElse(-1);
    }

    /* ------------
       Common utils
       ------------ */

    public static int getIndentWidth() {
        return NumberUtils.toInt(
            System.getProperty(PROPERTY_INDENT),
            XmlComparatorConstants.DEFAULT_LOG_INDENT_WIDTH);
    }

    public static String getIndent() {
        return StringUtils.repeat(StringUtils.SPACE, getIndentWidth());
    }

    public static int getLogColumnWidth() {
        int logTableWidth = NumberUtils.toInt(
            System.getProperty(PROPERTY_WIDTH),
            XmlComparatorConstants.DEFAULT_LOG_TABLE_WIDTH);
        return (logTableWidth - getIndentWidth() - XmlComparatorConstants.LOG_COLUMN_SEPARATOR.length()) / 2;
    }

}
