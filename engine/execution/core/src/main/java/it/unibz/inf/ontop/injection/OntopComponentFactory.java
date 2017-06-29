package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.owlrefplatform.core.DBConnector;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.answering.reformulation.OntopQueryReformulator;

public interface OntopComponentFactory {

    OntopQueryReformulator create(OBDASpecification obdaSpecification, ExecutorRegistry executorRegistry);

    DBConnector create(OntopQueryReformulator reformulator);
}