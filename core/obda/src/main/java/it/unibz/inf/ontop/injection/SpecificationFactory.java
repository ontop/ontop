package it.unibz.inf.ontop.injection;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;

/**
 * To be built by Guice (Assisted inject pattern)
 *
 * Accessible through Guice (recommended) or through MappingCoreSingletons.
 *
 */
public interface SpecificationFactory {

    PrefixManager createPrefixManager(ImmutableMap<String, String> prefixToURIMap);

    OBDASpecification createSpecification(Mapping saturatedMapping, DBParameters dbMParameters, ClassifiedTBox tBox);
}
