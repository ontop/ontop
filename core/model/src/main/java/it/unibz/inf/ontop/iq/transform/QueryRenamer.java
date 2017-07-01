package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.iq.IntermediateQuery;

public interface QueryRenamer extends QueryTransformer {

    @Override
    IntermediateQuery transform(IntermediateQuery originalQuery);
}
