package it.unibz.inf.ontop.injection;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;

public interface ProvenanceMappingFactory {

    MappingWithProvenance create(ImmutableMap<IntermediateQuery, PPMappingAssertionProvenance> provenanceMap,
                                 MappingMetadata mappingMetadata, ExecutorRegistry executorRegistry);
}
