package it.unibz.inf.ontop.pivotalrepr.datalog;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.injection.OntopModelFactory;
import it.unibz.inf.ontop.model.CQIE;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.MetadataForQueryOptimization;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;

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
                                                            OntopModelFactory modelFactory,
                                                            ExecutorRegistry executorRegistry) {

        return mappingsDR.keySet().stream()
                .map(predicate -> DatalogProgram2QueryConverter.convertDatalogDefinitions(metadataForQueryOptimization,
                        predicate, mappingsDR, tablePredicates, Optional.empty(), modelFactory, executorRegistry))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }


}
