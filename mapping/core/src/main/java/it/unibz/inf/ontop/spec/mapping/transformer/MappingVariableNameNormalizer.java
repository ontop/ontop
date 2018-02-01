package it.unibz.inf.ontop.spec.mapping.transformer;


import it.unibz.inf.ontop.spec.mapping.Mapping;

/**
 * Renames variables,
 * so that two mapping source queries do not share a variable name
 */
public interface MappingVariableNameNormalizer {

    Mapping normalize(Mapping mapping);
}
