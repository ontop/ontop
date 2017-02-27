package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;

/**
 * Factory following the Guice AssistedInject pattern.
 *
 * See https://github.com/google/guice/wiki/AssistedInject.
 */
public interface IntermediateQueryFactory {

    IntermediateQueryBuilder createIQBuilder(DBMetadata metadata, ExecutorRegistry executorRegistry);

}
