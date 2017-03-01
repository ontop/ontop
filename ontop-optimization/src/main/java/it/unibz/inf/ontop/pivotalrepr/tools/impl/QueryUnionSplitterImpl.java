package it.unibz.inf.ontop.pivotalrepr.tools.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.tools.QueryUnionSplitter;

import java.util.stream.Stream;

/**
 * Only splits according to the top union
 */
@Singleton
public class QueryUnionSplitterImpl implements QueryUnionSplitter {

    @Inject
    private QueryUnionSplitterImpl() {
    }

    @Override
    public Stream<IntermediateQuery> splitUnion(IntermediateQuery query) {
        throw new RuntimeException("TODO: split union");
    }
}
