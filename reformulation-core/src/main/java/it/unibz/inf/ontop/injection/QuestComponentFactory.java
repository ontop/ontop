package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.owlrefplatform.core.srcquerygeneration.NativeQueryGenerator;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;
import it.unibz.inf.ontop.reformulation.IRIDictionary;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.reformulation.OBDAQueryProcessor;

import java.util.Optional;

public interface QuestComponentFactory {

    OBDAQueryProcessor create(OBDASpecification obdaSpecification, Optional<IRIDictionary> iriDictionary,
                              ExecutorRegistry executorRegistry);

    NativeQueryGenerator create(DBMetadata metadata, Optional<IRIDictionary> iriDictionary);

    DBConnector create(OBDAQueryProcessor questProcessor);
}