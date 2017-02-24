package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.pivotalrepr.utils.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecification;

public interface OntopEngineFactory {

    OntopQueryEngine create(OBDASpecification obdaSpecification, ExecutorRegistry executorRegistry);
}
