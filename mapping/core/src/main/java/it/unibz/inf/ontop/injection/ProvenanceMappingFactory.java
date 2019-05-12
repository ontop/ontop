package it.unibz.inf.ontop.injection;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;

/**
 * Accessible through Guice (recommended) or through MappingCoreSingletons.
 */
public interface ProvenanceMappingFactory {

    MappingWithProvenance create(ImmutableMap<IQ, PPMappingAssertionProvenance> provenanceMap,
                                 MappingMetadata mappingMetadata);
}
