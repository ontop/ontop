package it.unibz.inf.ontop.iq.tools;


import it.unibz.inf.ontop.iq.IntermediateQuery;

import java.util.stream.Stream;

public interface QueryUnionSplitter {

    Stream<IntermediateQuery> splitUnion(IntermediateQuery query);
}
