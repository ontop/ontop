package it.unibz.inf.ontop.spec.mapping.transformer;


import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;

public interface MappingCanonicalTransformer {

    MappingWithProvenance rewrite(MappingWithProvenance mapping);
}
