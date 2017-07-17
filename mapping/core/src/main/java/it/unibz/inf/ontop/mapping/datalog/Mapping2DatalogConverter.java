package it.unibz.inf.ontop.mapping.datalog;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.pp.PPTriplesMapProvenance;

import java.util.stream.Stream;

public interface Mapping2DatalogConverter {

    Stream<CQIE> convert(Mapping mapping);

    ImmutableMap<CQIE,PPTriplesMapProvenance> convert(MappingWithProvenance mappingWithProvenance);
}
