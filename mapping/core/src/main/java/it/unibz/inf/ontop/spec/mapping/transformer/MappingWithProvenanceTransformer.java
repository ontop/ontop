package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;

public interface MappingWithProvenanceTransformer {

    MappingWithProvenance transform(MappingWithProvenance mapping) throws MappingException;
}
