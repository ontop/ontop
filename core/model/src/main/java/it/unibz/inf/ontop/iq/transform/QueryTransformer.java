package it.unibz.inf.ontop.iq.transform;

import it.unibz.inf.ontop.exception.QueryTransformationException;
import it.unibz.inf.ontop.iq.IntermediateQuery;

/**
 * Transforms an IntermediateQuery
 */
public interface QueryTransformer {

    IntermediateQuery transform(IntermediateQuery originalQuery) throws QueryTransformationException;
}
