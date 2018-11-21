package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;

public interface MappingDatatypeFiller extends MappingWithProvenanceTransformer {

    @Override
    MappingWithProvenance transform(MappingWithProvenance mapping)
            throws UnknownDatatypeException;
}
