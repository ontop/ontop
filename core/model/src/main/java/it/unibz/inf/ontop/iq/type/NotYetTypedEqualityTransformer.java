package it.unibz.inf.ontop.iq.type;

import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;

/**
 * Transforms the equalities coming from a DB query according to their types.
 * They become either strict or non-strict equalities
 */
public interface NotYetTypedEqualityTransformer extends IQTreeTransformer {
}
