package it.unibz.inf.ontop.spec.mapping.transformer;


import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;

public interface MappingCanonicalTransformer extends MappingWithProvenanceTransformer {

    @Override
    MappingWithProvenance transform(MappingWithProvenance mapping);
}
