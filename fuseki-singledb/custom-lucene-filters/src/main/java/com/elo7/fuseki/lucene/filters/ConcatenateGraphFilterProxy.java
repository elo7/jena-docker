package com.elo7.fuseki.lucene.filters;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.ConcatenateGraphFilter;

import java.io.IOException;

public class ConcatenateGraphFilterProxy extends TokenFilter {
    public ConcatenateGraphFilterProxy(TokenStream input) {
        super(new ConcatenateGraphFilter(input));
    }

    public boolean incrementToken() throws IOException {
        return super.input.incrementToken();
    }
}
