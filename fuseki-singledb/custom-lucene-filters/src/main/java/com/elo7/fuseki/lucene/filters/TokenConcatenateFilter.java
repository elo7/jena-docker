/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.elo7.fuseki.lucene.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

/**
 * Filter outputs a single token which is a concatenation of the sorted and de-duplicated set of
 * input tokens. This can be useful for clustering/linking use cases.
 */
public class TokenConcatenateFilter extends TokenFilter {

    public static final int DEFAULT_MAX_OUTPUT_TOKEN_SIZE = 1024;
    public static final char DEFAULT_SEPARATOR = ' ';
    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final PositionIncrementAttribute posIncrAtt =
            addAttribute(PositionIncrementAttribute.class);
    private final PositionLengthAttribute posLenAtt = addAttribute(PositionLengthAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    private final int maxOutputTokenSize;
    private AttributeSource.State finalState;

    private final char separator;
    private boolean inputEnded = false;

    /** Create a new FingerprintFilter with default settings */
    public TokenConcatenateFilter(TokenStream input) {
        this(input, DEFAULT_MAX_OUTPUT_TOKEN_SIZE, DEFAULT_SEPARATOR);
    }

    /**
     * Create a new FingerprintFilter with control over all settings
     *
     * @param input the source of tokens to be summarized into a single token
     * @param maxOutputTokenSize the maximum length of the summarized output token. If exceeded, no
     *     output token is emitted
     * @param separator the character used to separate tokens combined into the single output token
     */
    public TokenConcatenateFilter(TokenStream input, int maxOutputTokenSize, char separator) {
        super(input);
        this.maxOutputTokenSize = maxOutputTokenSize;
        this.separator = separator;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (inputEnded) {
            return false;
        }
        boolean result = buildSingleOutputToken();
        finalState = captureState();
        return result;
    }

    /**
     * Gathers all tokens from input, de-duplicates, sorts then concatenates.
     *
     * @return false for end of stream; true otherwise
     */
    private final boolean buildSingleOutputToken() throws IOException {
        inputEnded = false;

        char[] clonedLastTerm = null;

        int outputTokenSize = 0;

        // Set the attributes for the single output token

        List<char[]> tokens = new ArrayList<>();

        while (input.incrementToken()) {
            if (outputTokenSize > maxOutputTokenSize) {
                continue;
            }

            final char[] term = termAttribute.buffer();
            final int length = termAttribute.length();

            clonedLastTerm = new char[length];
            System.arraycopy(term, 0, clonedLastTerm, 0, length);

            if (tokens.size() > 1) {
                outputTokenSize++;
            }

            tokens.add(clonedLastTerm);
            outputTokenSize += length;
        }
        // Force end-of-stream operations to get the final state.
        input.end();
        inputEnded = true;

        // Gathering complete - now output exactly zero or one token:

        offsetAtt.setOffset(0, offsetAtt.endOffset());
        posLenAtt.setPositionLength(1);
        posIncrAtt.setPositionIncrement(1);
        typeAtt.setType("fingerprint");

        // No tokens gathered - no output
        if (tokens.size() < 1) {
            termAttribute.setEmpty();
            return false;
        }

        // Tokens gathered are too large - no output
        if (outputTokenSize > maxOutputTokenSize) {
            termAttribute.setEmpty();
            tokens.clear();
            return false;
        }

        // Special case - faster option when we have a single token
        if (tokens.size() == 1) {
            termAttribute.setEmpty().append(new String(clonedLastTerm));
            tokens.clear();
            return true;
        }

        StringBuilder sb = new StringBuilder();
        // TODO lets append directly to termAttribute?
        for (Object item : tokens) {
            if (sb.length() >= 1) {
                sb.append(separator);
            }
            sb.append((char[]) item);
        }
        termAttribute.setEmpty().append(sb);
        tokens.clear();
        return true;
    }

    @Override
    public final void end() throws IOException {
        if (!inputEnded) {
            // Rare case - If an IOException occurs while performing buildSingleOutputToken
            // we may not have called input.end() already
            input.end();
            inputEnded = true;
        }

        if (finalState != null) {
            restoreState(finalState);
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        inputEnded = false;
    }
}
