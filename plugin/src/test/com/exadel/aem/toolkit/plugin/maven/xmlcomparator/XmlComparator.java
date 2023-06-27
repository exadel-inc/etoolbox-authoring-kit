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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import com.exadel.aem.toolkit.core.CoreConstants;

public class XmlComparator {
    private static final Logger LOG = LoggerFactory.getLogger(XmlComparator.class);

    private final String path;
    private final String expected;
    private final String actual;
    private final Diff diff;

    private static final String LOG_ENTRY_PREFIX = "[%s] ";


    public XmlComparator(String path, String expected, String actual) {
        this.path = path;
        this.expected = expected;
        this.actual = actual;
        diff = DiffBuilder
            .compare(Input.fromString(expected))
            .withTest(Input.fromString(actual))
            .ignoreComments()
            .normalizeWhitespace()
            .build();
    }

    public boolean isEqual() {
        return !diff.hasDifferences();
    }

    public void logDiff()
        throws IOException, SAXException, ParserConfigurationException {

        if (isEqual()) {
            return;
        }

        StringBuilder logBuilder = new StringBuilder("difference(s) detected at ")
            .append(path)
            .append(System.lineSeparator());

        int differencesCount = 0;

        for (List<Difference> group : groupDifferences(diff)) {
            logDiff(group, logBuilder);
            differencesCount += group.size();
        }

        if (differencesCount > 0) {
            LOG.warn("{} {}", differencesCount, logBuilder);
        }
    }

    private void logDiff(
        List<Difference> differences,
        StringBuilder logBuilder)
        throws ParserConfigurationException, IOException, SAXException {

        if (differences == null || differences.isEmpty()) {
            return;
        }

        Comparison firstComparison = differences.get(0).getComparison();
        String expectedXPath = firstComparison.getControlDetails().getXPath();
        String actualXPath = firstComparison.getTestDetails().getXPath();
        List<Line> expectedLines = LineUtil.getLines(getDocument(expected), expectedXPath);
        List<Line> actualLines = LineUtil.getLines(getDocument(actual), actualXPath);
        LineUtil.align(expectedLines, actualLines);

        for (Difference difference : differences) {
            Comparison currentComparison = difference.getComparison();
            logBuilder
                .append(System.lineSeparator())
                .append(String.format(LOG_ENTRY_PREFIX, currentComparison.getType()))
                .append(currentComparison)
                .append(System.lineSeparator());
            LineUtil.highlight(
                currentComparison.getType(),
                expectedLines,
                currentComparison.getControlDetails().getXPath(),
                actualLines,
                currentComparison.getTestDetails().getXPath());
        }
        logDiff(expectedLines, actualLines, logBuilder);
    }

    private void logDiff(List<Line> expectedLines, List<Line> actualLines, StringBuilder logBuilder) {
        logBuilder
            .append(System.lineSeparator())
            .append(path)
            .append(System.lineSeparator())
            .append(System.lineSeparator())
            .append(StringUtils.repeat(CoreConstants.SEPARATOR_HYPHEN, LineUtil.getLogColumnWidth()))
            .append(XmlComparatorConstants.LOG_COLUMN_SEPARATOR)
            .append(StringUtils.repeat(CoreConstants.SEPARATOR_HYPHEN, LineUtil.getLogColumnWidth()))
            .append(System.lineSeparator())
            .append(StringUtils.center("Expected", LineUtil.getLogColumnWidth()))
            .append(XmlComparatorConstants.LOG_COLUMN_SEPARATOR)
            .append(StringUtils.center("Actual", LineUtil.getLogColumnWidth()))
            .append(System.lineSeparator())
            .append(StringUtils.repeat(CoreConstants.SEPARATOR_HYPHEN, LineUtil.getLogColumnWidth()))
            .append(XmlComparatorConstants.LOG_COLUMN_SEPARATOR)
            .append(StringUtils.repeat(CoreConstants.SEPARATOR_HYPHEN, LineUtil.getLogColumnWidth()))
            .append(System.lineSeparator());

        assert expectedLines.size() == actualLines.size();
        Iterator<Line> expectedLinesIterator = expectedLines.iterator();
        Iterator<Line> actualLinesIterator = actualLines.iterator();
        while (expectedLinesIterator.hasNext()) {
            Line expectedLine = expectedLinesIterator.next();
            Line actualLine = actualLinesIterator.next();
            if (expectedLine.isTopIndent() && actualLine.isTopIndent()) {
                logBuilder.append(System.lineSeparator());
            }
            logBuilder
                .append(expectedLine.pad(LineUtil.getLogColumnWidth()))
                .append(XmlComparatorConstants.LOG_COLUMN_SEPARATOR)
                .append(actualLine.pad(LineUtil.getLogColumnWidth()))
                .append(System.lineSeparator());
            if (expectedLine.isBottomIndent() && actualLine.isBottomIndent()) {
                logBuilder.append(System.lineSeparator());
            }
        }
        logBuilder.append(System.lineSeparator());
    }

    private static Document getDocument(String content) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(content)));
    }

    private static Collection<List<Difference>> groupDifferences(Diff diff) {
        Map<String, List<Difference>> groups = new LinkedHashMap<>();
        for (Difference difference : diff.getDifferences()) {
            String expectedNodePath = StringUtils.substringBeforeLast(
                difference.getComparison().getControlDetails().getXPath(),
                XmlComparatorConstants.ATTRIBUTE_SEPARATOR);
            String actualNodePath = StringUtils.substringBeforeLast(
                difference.getComparison().getTestDetails().getXPath(),
                XmlComparatorConstants.ATTRIBUTE_SEPARATOR);
            String effectivePath;
            if (difference.getComparison().getType() == ComparisonType.CHILD_LOOKUP
                && StringUtils.isAnyEmpty(expectedNodePath, actualNodePath)) {
                effectivePath = Stream.of(expectedNodePath, actualNodePath)
                    .filter(StringUtils::isNotEmpty)
                    .map(path -> StringUtils.substringBeforeLast(path, CoreConstants.SEPARATOR_SLASH))
                    .findFirst()
                    .orElse(StringUtils.EMPTY);
            } else if (StringUtils.equals(expectedNodePath, actualNodePath)) {
                effectivePath = expectedNodePath;
            } else {
                effectivePath = expectedNodePath + CoreConstants.SEPARATOR_COMMA + actualNodePath;
            }
            groups.computeIfAbsent(effectivePath, key -> new ArrayList<>()).add(difference);
        }
        return groups.values();
    }
}
