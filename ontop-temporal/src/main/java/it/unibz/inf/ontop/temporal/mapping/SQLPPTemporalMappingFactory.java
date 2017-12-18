package it.unibz.inf.ontop.temporal.mapping;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;


public interface SQLPPTemporalMappingFactory {

    SQLPPTemporalMapping createSQLPreProcessedMapping(ImmutableList<SQLPPTemporalTriplesMap> newMappings,
                                              MappingMetadata metadata) throws DuplicateMappingException;

}
