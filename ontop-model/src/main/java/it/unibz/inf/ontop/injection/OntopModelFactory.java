package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.pivotalrepr.IntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.MetadataForQueryOptimization;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;

/**
 * Factory following the Guice AssistedInject pattern.
 *
 * See https://github.com/google/guice/wiki/AssistedInject.
 */
public interface OntopModelFactory {

    IntermediateQueryBuilder create(MetadataForQueryOptimization metadata,
                                    ExecutorRegistry executorRegistry);

}
