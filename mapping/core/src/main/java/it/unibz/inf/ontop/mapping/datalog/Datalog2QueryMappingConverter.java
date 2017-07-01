package it.unibz.inf.ontop.mapping.datalog;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;

import java.util.List;
import java.util.stream.Stream;

public interface Datalog2QueryMappingConverter {

    Mapping convertMappingRules(ImmutableList<CQIE> mappingRules, DBMetadata dbMetadata,
                                ExecutorRegistry executorRegistry, MappingMetadata mappingMetadata);
}
