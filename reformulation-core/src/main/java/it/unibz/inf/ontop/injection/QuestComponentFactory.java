package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.answering.reformulation.OntopQueryReformulator;

public interface QuestComponentFactory {

    OntopQueryReformulator create(OBDASpecification obdaSpecification, ExecutorRegistry executorRegistry);

    NativeQueryGenerator create(DBMetadata metadata);

    DBConnector create(OntopQueryReformulator questProcessor);
}