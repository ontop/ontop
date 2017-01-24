package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.reformulation.OBDAQueryProcessor;

public interface QuestComponentFactory {

    OBDAQueryProcessor create(OBDASpecification obdaSpecification, ExecutorRegistry executorRegistry);

    NativeQueryGenerator create(DBMetadata metadata);
    NativeQueryGenerator create(DBMetadata metadata, SemanticIndexURIMap uriRefIds);

    DBConnector create(OBDAQueryProcessor questProcessor);
}