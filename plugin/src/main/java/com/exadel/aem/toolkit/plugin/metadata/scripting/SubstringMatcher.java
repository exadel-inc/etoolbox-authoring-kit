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
package com.exadel.aem.toolkit.plugin.metadata.scripting;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Parses a string of text and extracts substring starting (and, optionally, ending) with a given token
 */
class SubstringMatcher {

    private static final List<Character> QUOTE_CHARS = Arrays.asList('\'', '"', '`');
    private static final char ESCAPE_CHAR = '\\';

    private final String source;
    private final String startToken;
    private final String endToken;
    private final List<String> prefixes;

    private String lastMatch;
    private int cursor;

    /**
     * Creates a new {@link SubstringMatcher} instance from the specified source string and the marker of substrings
     * @param source The string that is used as the source for matching substrings
     * @param marker Starting token that signifies a substring. A non-empty string is expected
     */
    public SubstringMatcher(String source, String marker) {
        this(source, marker, null);
    }

    /**
     * Creates a new {@link SubstringMatcher} instance from the specified source string, and also start token and end
     * token that delimit substrings
     * @param source     The string that is used as the source for matching substrings
     * @param startToken Specifies where the substring starts. A non-empty string is expected
     * @param endToken   Specifies where the substring ends. A non-empty string is expected
     */
    public SubstringMatcher(String source, String startToken, String endToken) {
        this(source, startToken, endToken, null);
    }

    /**
     * Creates a new instance from the specified source string, start token, and end token that delimit substrings and
     * possible prefixes that precede the start token
     * @param source     The string that is used as the source for matching substrings
     * @param startToken Specifies where the substring starts. A non-empty string is expected
     * @param endToken   Specifies where the substring ends. A non-empty string is expected
     * @param prefixes   List of string prefixes that can come before the start token.
     */
    public SubstringMatcher(String source, String startToken, String endToken, List<String> prefixes) {
        this.source = source;
        this.startToken = startToken;
        this.endToken = endToken;
        this.prefixes = prefixes;
    }

    /**
     * Retrieves the next substring from the source that starts and ends with the specified tokens. Checks for an active
     * prefix if set for the instance
     * @return The next available {@link Substring}, or {@code null} if none available
     */
    public Substring next() {
        if (cursor > source.length() || StringUtils.isEmpty(startToken)) {
            return null;
        }
        Substring nextSubstring = endToken != null ? nextSubstring() : nextWord();
        if (nextSubstring == null) {
            return null;
        }
        if (prefixes != null) {
            String activePrefix = prefixes
                .stream()
                .filter(prefix ->
                    nextSubstring.getStart() >= prefix.length()
                        && source.startsWith(prefix, nextSubstring.getStart() - prefix.length()))
                .findFirst()
                .orElse(null);
            if (activePrefix == null) {
                return next();
            }
            boolean isNotEscaped = nextSubstring.getStart() - activePrefix.length() == 0
                || source.charAt(nextSubstring.getStart() - activePrefix.length() - 1) != ESCAPE_CHAR;
            return isNotEscaped ? nextSubstring.adjustStart(nextSubstring.getStart() - activePrefix.length()) : next();
        }
        return nextSubstring.getStart() == 0 || source.charAt(nextSubstring.getStart() - 1) != ESCAPE_CHAR
            ? nextSubstring
            : next();
    }

    /**
     * Retrieves the next substring from the source that is enclosed by the start and end token
     * @return The next available {@link Substring}, or {@code null} if none available
     */
    private Substring nextSubstring() {
        advanceBeyond(startToken);
        if (cursor > source.length()) {
            return null;
        }
        int startPosition = cursor - lastMatch.length();
        int numOfStartTokens = 1;
        int numOfEndTokens = 0;

        while (numOfStartTokens != numOfEndTokens) {
            advanceBeyond(startToken, endToken);
            if (cursor > source.length()) {
                return null;
            }
            if (StringUtils.equalsAny(lastMatch, startToken)) {
                numOfStartTokens++;
            } else {
                numOfEndTokens++;
            }
        }
        return new Substring(source, startPosition, cursor);
    }

    /**
     * Retrieves the next word from the source that is preceded by the given start token
     * @return The next available {@link Substring}, or {@code null} if none available
     */
    private Substring nextWord() {
        advanceBeyond(startToken);
        if (cursor > source.length()) {
            return null;
        }
        int startPosition = cursor - lastMatch.length();
        int endPosition = cursor;
        while (endPosition < source.length()
            && (Character.isLetterOrDigit(source.charAt(endPosition)) || source.charAt(endPosition) == '_')) {
            endPosition++;
        }
        cursor = endPosition;
        if (endPosition - startPosition == lastMatch.length()) {
            return nextWord();
        }
        return new Substring(source, startPosition, endPosition);
    }

    /**
     * Moves the beyond any of the provided tokens if found in the source string, and handles escaped and quoted tokens
     * @param anyOfTokens Token values that the cursor should move past
     */
    private void advanceBeyond(String... anyOfTokens) {
        Pair<String, Integer> nextToken = Arrays
            .stream(anyOfTokens)
            .map(token -> Pair.of(token, source.indexOf(token, cursor)))
            .filter(pair -> pair.getRight() >= cursor)
            .min(Comparator.comparingInt(Pair::getRight))
            .orElse(null);
        if (nextToken == null) {
            cursor = source.length() + 1;
            lastMatch = null;
            return;
        }
        int quotePosition = QUOTE_CHARS
            .stream()
            .mapToInt(chr -> source.indexOf(chr, cursor))
            .filter(index -> index >= cursor)
            .min()
            .orElse(-1);
        if (quotePosition >= cursor && quotePosition < nextToken.getRight()) {
            char activeQuote = source.charAt(quotePosition);
            int pairedQuotePosition = source.indexOf(activeQuote, quotePosition + 1);
            if (pairedQuotePosition > quotePosition && nextToken.getRight() < pairedQuotePosition) {
                cursor = pairedQuotePosition + 1;
                advanceBeyond(anyOfTokens);
                return;
            }
        }
        lastMatch = nextToken.getLeft();
        cursor = nextToken.getLeft().length() + nextToken.getRight();
    }

    /**
     * Represents a portion of the source string from a specified start index to an end index Start index is inclusive,
     * while the end index is exclusive
     */
    public static class Substring {
        private final String source;
        private int start;
        private final int end;

        /**
         * Creates a new instance with the specified source, start index, and end index
         * @param source   The string that is used as the source for matching substrings
         * @param start    Specifies the starting index of the substring
         * @param endIndex Specifies the ending index of the substring
         */
        public Substring(String source, int start, int endIndex) {
            this.source = source;
            this.start = start;
            this.end = endIndex;
        }

        /**
         * Retrieves the starting index of this substring (inclusive)
         * @return Integer value
         */
        public int getStart() {
            return start;
        }

        /**
         * Updates the start index of this substring and returns this substring for chaining
         * @param start Specifies the starting index of the substring
         * @return This substring with the updated start
         */
        private Substring adjustStart(int start) {
            this.start = start;
            return this;
        }

        /**
         * Retrieves the ending index of this substring (exclusive)
         * @return Integer value
         */
        public int getEnd() {
            return end;
        }

        /**
         * Retrieves the content from the source string enclosed by this substring
         * @return A new string that is a substring of the source string
         */
        public String getContent() {
            return source.substring(start, end);
        }
    }
}
