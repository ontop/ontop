package it.unibz.inf.ontop.injection;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;

/**
 * Accessible through Guice (recommended) or through MappingCoreSingletons.
 */
public interface ProvenanceMappingFactory {

    MappingWithProvenance create(ImmutableList<MappingAssertion> assertion);
}
