package it.unibz.inf.ontop.iq.transformer;

import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;

/**
 * Pushes down the expression as much as possible without creating new sub-queries
 * (e.g. without creating a FILTER on the left of a LJ).
 *
 * In particular, it pushes down expressions in existing sub-queries (for instance inside children of a UNION)
 *
 */
public interface BooleanExpressionPushDownTransformer extends IQTreeTransformer {
}
