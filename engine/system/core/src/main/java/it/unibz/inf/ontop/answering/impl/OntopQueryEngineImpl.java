package it.unibz.inf.ontop.answering.impl;


import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.reformulation.QueryTranslator;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.injection.OntopSystemFactory;
import it.unibz.inf.ontop.injection.OntopTranslationFactory;
import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.owlrefplatform.core.OntopConnection;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecification;

public class OntopQueryEngineImpl implements OntopQueryEngine {

    private final DBConnector dbConnector;

    @AssistedInject
    private OntopQueryEngineImpl(@Assisted OBDASpecification obdaSpecification,
                                 @Assisted ExecutorRegistry executorRegistry,
                                 OntopTranslationFactory translationFactory,
                                 OntopSystemFactory systemFactory) {
        QueryTranslator queryTranslator = translationFactory.create(obdaSpecification, executorRegistry);
        dbConnector = systemFactory.create(queryTranslator, obdaSpecification.getDBMetadata());
    }

    @Override
    public void close() throws OntopConnectionException {
        dbConnector.close();
    }

    @Override
    public OntopConnection getConnection() throws OntopConnectionException {
        return dbConnector.getConnection();
    }
}
