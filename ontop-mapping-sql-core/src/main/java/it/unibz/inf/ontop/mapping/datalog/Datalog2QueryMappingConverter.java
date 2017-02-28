package it.unibz.inf.ontop.mapping.datalog;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.model.CQIE;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;

public interface Datalog2QueryMappingConverter {

    Mapping convertMappingRules(ImmutableList<CQIE> mappingRules,
                                DBMetadata dbMetadata,
                                ExecutorRegistry executorRegistry, MappingMetadata mappingMetadata);
}
