package it.unibz.inf.ontop.answering.impl;


import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.reformulation.OntopQueryReformulator;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.injection.OntopComponentFactory;
import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.owlrefplatform.core.OntopConnection;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecification;

public class OntopQueryEngineImpl implements OntopQueryEngine {

    private final DBConnector dbConnector;

    @AssistedInject
    private OntopQueryEngineImpl(@Assisted OBDASpecification obdaSpecification,
                                 @Assisted ExecutorRegistry executorRegistry,
                                 OntopComponentFactory componentFactory) {
        OntopQueryReformulator queryReformulator = componentFactory.create(obdaSpecification, executorRegistry);
        dbConnector = componentFactory.create(queryReformulator);
    }

    @Override
    public boolean connect() throws OntopConnectionException {
        return dbConnector.connect();
    }

    @Override
    public void close() throws OntopConnectionException {
        dbConnector.close();
    }

    @Override
    public OntopConnection getNonPoolConnection() throws OntopConnectionException {
        return dbConnector.getNonPoolConnection();
    }

    @Override
    public OntopConnection getConnection() throws OntopConnectionException {
        return dbConnector.getConnection();
    }
}
