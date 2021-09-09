package it.unibz.inf.ontop.answering.impl;


import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.injection.OntopSystemFactory;
import it.unibz.inf.ontop.injection.ReformulationFactory;
import it.unibz.inf.ontop.answering.connection.DBConnector;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.spec.OBDASpecification;

public class OntopQueryEngineImpl implements OntopQueryEngine {

    private final DBConnector dbConnector;
    private final QueryReformulator queryReformulator;

    @AssistedInject
    private OntopQueryEngineImpl(@Assisted OBDASpecification obdaSpecification,
                                 ReformulationFactory translationFactory,
                                 OntopSystemFactory systemFactory) {
        queryReformulator = translationFactory.create(obdaSpecification);
        dbConnector = systemFactory.create(queryReformulator);
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
    public OntopConnection getConnection() throws OntopConnectionException {
        return dbConnector.getConnection();
    }

    @Override
    public QueryReformulator getQueryReformulator() {
        return queryReformulator;
    }
}
