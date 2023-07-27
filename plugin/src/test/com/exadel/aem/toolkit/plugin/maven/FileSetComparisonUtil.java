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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exadel.aem.toolkit.plugin.maven.xmlcomparator.XmlComparator;

class FileSetComparisonUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FileSetComparisonUtil.class);

    private FileSetComparisonUtil() {
    }

    static boolean compare(Map<String, String> actualFiles, Map<String, String> expectedFiles, String resourcePath) {
        if (!isSame(actualFiles, expectedFiles)) {
            LOG.error(
                "File sets differ: expected {}, received {}",
                Arrays.toString(expectedFiles.keySet().toArray()),
                Arrays.toString(actualFiles.keySet().toArray()));
            return false;
        }
        Collection<String> fileNames = expectedFiles.keySet();
        boolean result = true;
        for (String fileName : fileNames) {
            String actualContent = actualFiles.get(fileName);
            String expectedContent = expectedFiles.get(fileName);
            XmlComparator xmlComparator = new XmlComparator(
                resourcePath + File.separator + fileName,
                expectedContent,
                actualContent);
            if (xmlComparator.isEqual()) {
                continue;
            }
            try {
                xmlComparator.logDiff();
            } catch (Exception ex) {
                LOG.error("Could not implement XML files comparison", ex);
            }
            result = false;
        }
        return result;
    }

    private static boolean isSame(Map<String, String> first, Map<String, String> second) {
        if (first == null || second == null || first.size() != second.size()) {
            return false;
        }
        return first.keySet().stream().allMatch(second::containsKey);
    }
}
