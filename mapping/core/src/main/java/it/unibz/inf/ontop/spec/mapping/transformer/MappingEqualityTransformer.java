package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;

/**
 * Transforms the equalities coming from the mapping according to their types.
 * They become either strict or non-strict equalities
 */
public interface MappingEqualityTransformer {

    IQTree transform(IQTree tree);

    MappingAssertion transform(MappingAssertion tree);
}
