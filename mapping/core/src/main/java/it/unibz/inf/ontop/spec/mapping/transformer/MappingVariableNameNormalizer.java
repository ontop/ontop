package it.unibz.inf.ontop.spec.mapping.transformer;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;

/**
 * Renames variables,
 * so that two mapping source queries do not share a variable name
 */
public interface MappingVariableNameNormalizer {

    ImmutableList<MappingAssertion> normalize(ImmutableList<MappingAssertion> mapping);
}
