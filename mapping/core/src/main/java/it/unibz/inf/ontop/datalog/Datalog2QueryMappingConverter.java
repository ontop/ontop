package it.unibz.inf.ontop.datalog;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;

public interface Datalog2QueryMappingConverter {

    MappingWithProvenance convertMappingRules(ImmutableMap<CQIE, PPMappingAssertionProvenance> datalogMap, MappingMetadata mappingMetadata);
}
