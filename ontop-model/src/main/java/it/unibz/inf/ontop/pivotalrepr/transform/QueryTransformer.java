package it.unibz.inf.ontop.pivotalrepr.transform;

import it.unibz.inf.ontop.exception.QueryTransformationException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

/**
 * Transforms an IntermediateQuery
 */
public interface QueryTransformer {

    IntermediateQuery transform(IntermediateQuery originalQuery) throws QueryTransformationException;
}
