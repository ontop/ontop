package it.unibz.inf.ontop.spec.mapping.transformer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;

public interface MappingDatatypeFiller extends MappingWithProvenanceTransformer {

    @Override
    ImmutableList<MappingAssertion> transform(ImmutableList<MappingAssertion> mapping)
            throws UnknownDatatypeException;
}
