package it.unibz.inf.ontop.spec.mapping.transformer;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;

/**
 * Renames variables,
 * so that two mapping source queries do not share a variable name
 */
public interface MappingVariableNameNormalizer {

    ImmutableMap<MappingAssertionIndex, IQ> normalize(ImmutableMap<MappingAssertionIndex, IQ> mapping);
}
