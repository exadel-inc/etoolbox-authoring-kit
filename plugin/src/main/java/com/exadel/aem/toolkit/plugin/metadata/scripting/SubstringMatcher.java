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

class SubstringMatcher {

    private static final List<Character> QUOTE_CHARS = Arrays.asList('\'', '"', '`');
    private static final char ESCAPE_CHAR = '\\';

    private final String source;
    private final String startToken;
    private final String endToken;
    private final List<String> prefixes;

    private String lastMatch;
    private int cursor;

    public SubstringMatcher(String source, String startToken) {
        this(source, startToken, null);
    }

    public SubstringMatcher(String source, String startToken, String endToken) {
        this(source, startToken, endToken, null);
    }

    public SubstringMatcher(String source, String startToken, String endToken, List<String> prefixes) {
        this.source = source;
        this.startToken = startToken;
        this.endToken = endToken;
        this.prefixes = prefixes;
    }

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

    public static class Substring {
        private final String source;
        private int start;
        private final int end;

        public Substring(String source, int start, int endIndex) {
            this.source = source;
            this.start = start;
            this.end = endIndex;
        }

        public int getStart() {
            return start;
        }

        private Substring adjustStart(int start) {
            this.start = start;
            return this;
        }

        public int getEnd() {
            return end;
        }

        public String getContent() {
            return source.substring(start, end);
        }
    }
}
