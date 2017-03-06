package it.unibz.inf.ontop.pivotalrepr.transform;

import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

public interface QueryRenamer extends QueryTransformer {

    @Override
    IntermediateQuery transform(IntermediateQuery originalQuery);
}
