package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;

/**
 * Following the Guice AssistedInject pattern
 */
public interface ReformulationFactory {

    QueryUnfolder create(Mapping mapping);

    NativeQueryGenerator create(DBMetadata metadata);
}
