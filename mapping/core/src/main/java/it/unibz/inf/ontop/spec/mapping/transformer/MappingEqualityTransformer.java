package it.unibz.inf.ontop.spec.mapping.transformer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;

/**
 * Transforms the equalities coming from the mapping according to their types.
 * They become either strict or non-strict equalities
 */
public interface MappingEqualityTransformer extends MappingWithProvenanceTransformer {

    @Override
    ImmutableList<MappingAssertion> transform(ImmutableList<MappingAssertion> mapping);
}
