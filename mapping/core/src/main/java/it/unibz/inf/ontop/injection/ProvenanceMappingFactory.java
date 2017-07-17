package it.unibz.inf.ontop.injection;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.pp.PPTriplesMapProvenance;

public interface ProvenanceMappingFactory {

    MappingWithProvenance create(ImmutableMap<IntermediateQuery, PPTriplesMapProvenance> provenanceMap,
                                 MappingMetadata mappingMetadata, ExecutorRegistry executorRegistry);
}
