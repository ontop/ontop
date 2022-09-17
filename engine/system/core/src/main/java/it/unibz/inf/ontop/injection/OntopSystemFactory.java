package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.connection.DBConnector;
import it.unibz.inf.ontop.spec.OBDASpecification;

public interface OntopSystemFactory {

    DBConnector create(QueryReformulator translator);

    OntopQueryEngine create(OBDASpecification obdaSpecification);
}
