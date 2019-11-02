package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;

/**
 * Transforms the equalities coming from the mapping according to their types.
 * They become either strict or non-strict equalities
 */
public interface MappingEqualityTransformer extends MappingWithProvenanceTransformer {

    @Override
    MappingWithProvenance transform(MappingWithProvenance mapping);
}
