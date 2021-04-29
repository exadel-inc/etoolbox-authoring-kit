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
package com.exadel.aem.toolkit.plugin.targets;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.plugin.utils.DialogConstants;

/**
 * Helper class containing methods to manage and traverse relative paths by which {@link Target} instances are related
 * to each other
 */
class PathSplitHelper {

    private final String path;
    private List<Pair<Integer, Integer>> escapedSequences;

    /**
     * Instantiation-restricting constructor
     * @param path Path value, non-blank string expected
     */
    private PathSplitHelper(String path) {
        this.path = path;
        if (path == null
            || !path.contains(CoreConstants.SEPARATOR_SLASH)
            || !path.contains(DialogConstants.DOUBLE_QUOTE)) {
            return;
        }
        escapedSequences = getEscapedSequences(path);
    }

    /**
     * Gets whether the path associated with this instance can be split into chunks
     * @return True or false
     */
    boolean isSplittable() {
        if (escapedSequences == null || escapedSequences.isEmpty()) {
            return StringUtils.contains(path, CoreConstants.SEPARATOR_SLASH);
        }
        int cursor = path.indexOf(CoreConstants.SEPARATOR_SLASH);
        while (cursor >= 0) {
            int position = cursor;
            if (escapedSequences.stream().noneMatch(pair -> position > pair.getLeft() && position < pair.getRight())) {
                return true;
            }
            cursor = path.indexOf(CoreConstants.SEPARATOR_SLASH, cursor + 1);
        }
        return false;
    }

    /**
     * Retrieves a sequence of path chunks
     * @return {@code Queue} object
     */
    Queue<String> getChunks() {
        if (path == null) {
            return new LinkedList<>();
        }
        if (escapedSequences == null || escapedSequences.isEmpty()) {
            return Pattern.compile(CoreConstants.SEPARATOR_SLASH).splitAsStream(path).collect(Collectors.toCollection(LinkedList::new));
        }

        Queue<String> result = new LinkedList<>();
        List<Integer> splitPositions = new ArrayList<>();
        int cursor = path.indexOf(CoreConstants.SEPARATOR_SLASH);
        while (cursor >= 0) {
            int position = cursor;
            if (escapedSequences.stream().noneMatch(pair -> position > pair.getLeft() && position < pair.getRight())) {
                splitPositions.add(cursor);
            }
            cursor = path.indexOf(CoreConstants.SEPARATOR_SLASH, cursor + 1);
        }
        if (splitPositions.isEmpty()) {
            result.add(path);
            return result;
        }
        for (int i = 0; i < splitPositions.size(); i++) {
            if (i == 0) {
                result.add(path.substring(0, splitPositions.get(i)));
            } else {
                result.add(path.substring(splitPositions.get(i - 1) + 1, splitPositions.get(i)));
            }
            if (i == splitPositions.size() - 1) {
                result.add(path.substring(splitPositions.get(i) + 1));
            }
        }
        return result;
    }

    /**
     * Finds in the provided {@code path} char sequences that are "escaped" from splitting despite containing
     * a split character
     * @param path Path value
     * @return {@code List} of numeric pairs, each representing the starting and ending position of an escaped sequence
     */
    private static List<Pair<Integer, Integer>> getEscapedSequences(String path) {
        List<Pair<Integer, Integer>> result = new ArrayList<>();
        int opening = path.indexOf(DialogConstants.DOUBLE_QUOTE);
        while (opening >= 0) {
            int closing = path.indexOf(DialogConstants.DOUBLE_QUOTE, opening + 1);
            if (closing < 0) {
                return result;
            }
            result.add(Pair.of(opening, closing));
            opening = path.indexOf(DialogConstants.DOUBLE_QUOTE, closing + 1);
        }
        return result;
    }

    /**
     * Initializes a class instance with the absolute or relative paths specified
     * @param path Path value, non-blank string expected
     */
    static PathSplitHelper of(String path) {
        return new PathSplitHelper(path);
    }
}
