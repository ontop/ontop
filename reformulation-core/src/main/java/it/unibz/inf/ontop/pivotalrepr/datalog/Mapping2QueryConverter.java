package it.unibz.inf.ontop.pivotalrepr.datalog;

import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.model.CQIE;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.MetadataForQueryOptimization;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Convert mappings from Datalog to IntermediateQuery
 *
 */
public class Mapping2QueryConverter {

    public static Stream<IntermediateQuery> convertMappings(ImmutableMultimap<Predicate, CQIE> mappingsDR,
                                                            Collection<Predicate> tablePredicates,
                                                            MetadataForQueryOptimization metadataForQueryOptimization,
                                                            Injector injector) {

        return mappingsDR.keySet().stream()
                .map(predicate -> DatalogProgram2QueryConverter.convertDatalogDefinitions(metadataForQueryOptimization,
                        predicate, mappingsDR, tablePredicates, Optional.empty(), injector))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }


}
