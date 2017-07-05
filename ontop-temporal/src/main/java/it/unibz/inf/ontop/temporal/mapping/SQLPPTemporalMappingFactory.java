package it.unibz.inf.ontop.temporal.mapping;


import com.google.common.collect.ImmutableList;

import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.model.*;


public interface SQLPPTemporalMappingFactory {

    SQLPPTemporalMapping createSQLPreProcessedMapping(ImmutableList<SQLPPTemporalMappingAxiom> newMappings,
                                              MappingMetadata metadata) throws DuplicateMappingException;

}
