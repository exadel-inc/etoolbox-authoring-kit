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
package com.exadel.aem.toolkit.regression;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;

import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.etoolbox.coconut.Comparator;
import com.exadel.etoolbox.coconut.OutputType;
import com.exadel.etoolbox.coconut.diff.Diff;
import com.exadel.etoolbox.coconut.filter.Filter;

class ComparisonUtil {

    private static final Filter PACKAGE_FILTER = new ContentPackageFilter();

    private ComparisonUtil() {
    }

    static boolean isMatch(Path oldDirectory, Path newDirectory) {
        Pair<Path[], Path[]> comparable = getComparable(oldDirectory, newDirectory);
        Path[] oldPackages = comparable.getLeft();
        Path[] newPackages = comparable.getRight();
        if (ArrayUtils.isEmpty(oldPackages) && ArrayUtils.isEmpty(newPackages)) {
            RegressionTest.LOG.info("There are no different packages");
            return true;
        }
        List<Diff> differences = new Comparator()
            .left(oldPackages, "Expected")
            .right(newPackages, "Actual")
            .filter(PACKAGE_FILTER)
            .compare();

        boolean result = Comparator.isMatch(differences);
        if (!differences.isEmpty()) {
            report(differences);
        }
        return result;
    }

    private static Pair<Path[], Path[]> getComparable(Path oldPackages, Path newPackages) {
        File[] oldFiles = oldPackages.toFile().listFiles();
        File[] newFiles = newPackages.toFile().listFiles();
        Assert.assertTrue("Pre-change content packages are missing", ArrayUtils.isNotEmpty(oldFiles));
        Assert.assertTrue("Post-change content packages are missing", ArrayUtils.isNotEmpty(newFiles));

        assert oldFiles != null;
        assert newFiles != null;
        Assert.assertEquals(
            "Sets of packages from the pre-change and post-change builds are different",
            oldFiles.length,
            newFiles.length);

        List<Path> oldFilesFiltered = new ArrayList<>();
        List<Path> newFilesFiltered = new ArrayList<>();

        for (File oldFile : oldFiles) {
            if (StringUtils.containsIgnoreCase(oldFile.getName(), ".all-")) {
                continue;
            }
            long oldHash = getHashCode(oldFile);

            File newFile = Arrays
                .stream(newFiles)
                .filter(f -> f.getName().equals(oldFile.getName()))
                .findFirst()
                .orElse(null);
            long newHash = getHashCode(newFile);
            if (newHash != 0 && newHash != oldHash) {
                assert newFile != null;
                oldFilesFiltered.add(oldFile.toPath());
                newFilesFiltered.add(newFile.toPath());
            }
        }

        return Pair.of(oldFilesFiltered.toArray(new Path[0]), newFilesFiltered.toArray(new Path[0]));
    }

    private static long getHashCode(File value) {
        if (value == null) {
            return 0L;
        }
        ByteSource byteSource =  com.google.common.io.Files.asByteSource(value);
        try {
            return byteSource.hash(Hashing.sha256()).asLong();
        } catch (IOException e) {
            throw new AssertionError("Could not calculate the hash code " + e.getMessage());
        }
    }

    private static void report(List<Diff> differences) {
        StringBuilder builder = new StringBuilder();
        for (Diff difference : differences) {
            String pathSeparator = File.separator;
            String fileName = StringUtils.substringAfterLast(difference.getLeft(), pathSeparator);
            builder.append(String.format(
                "\n\nFound %d difference(-s) in %s\n\n",
                difference.getCount(),
                fileName));
            builder.append(difference.toString(OutputType.CONSOLE));
        }
        RegressionTest.LOG.warn(builder.toString());
    }

    private static class ContentPackageFilter implements Filter {

        private static final String[] LOGGED_EXTENSIONS = {"info"};
        private static final String[] SKIPPED_EXTENSIONS = {"jar", "mf", "properties", "zip"};
        private static final String[] SKIPPED_FILE_NAMES = {"vault/properties.xml", "vault/filter.xml"};

        @Override
        public boolean skipDiff(Diff value) {
            boolean isSkippedExtension = anyMatches(
                value.getLeft(),
                value.getRight(),
                v -> StringUtils.equalsAny(getExtension(v), SKIPPED_EXTENSIONS));
            boolean isSkippedFileName = anyMatches(
                value.getLeft(),
                value.getRight(),
                v -> StringUtils.endsWithAny(v.toLowerCase(), SKIPPED_FILE_NAMES));
            return isSkippedExtension || isSkippedFileName;
        }

        @Override
        public boolean acceptDiff(Diff value) {
            return anyMatches(
                value.getLeft(),
                value.getRight(),
                v -> StringUtils.equalsAny(getExtension(v), LOGGED_EXTENSIONS));
        }

        private static boolean anyMatches(String left, String right, Predicate<String> predicate) {
            return predicate.test(left) || predicate.test(right);
        }

        private static String getExtension(String value) {
            return StringUtils.substringAfterLast(value, CoreConstants.SEPARATOR_DOT).toLowerCase();
        }
    }
}
