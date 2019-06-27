package it.unibz.inf.ontop.datalog;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;

import java.util.stream.Stream;

public interface Mapping2DatalogConverter {

    ImmutableMap<CQIE,PPMappingAssertionProvenance> convert(MappingWithProvenance mappingWithProvenance);
}
