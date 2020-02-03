package it.unibz.inf.ontop.spec.mapping.transformer;


import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;

/**
 * Renames variables,
 * so that two mapping source queries do not share a variable name
 */
public interface MappingVariableNameNormalizer {

    MappingInTransformation normalize(MappingInTransformation mapping);
}
