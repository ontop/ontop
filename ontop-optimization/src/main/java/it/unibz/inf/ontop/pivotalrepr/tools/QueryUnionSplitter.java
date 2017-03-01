package it.unibz.inf.ontop.pivotalrepr.tools;


import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

import java.util.stream.Stream;

public interface QueryUnionSplitter {

    Stream<IntermediateQuery> splitUnion(IntermediateQuery query);
}
