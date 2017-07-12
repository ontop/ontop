package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.answering.reformulation.QueryTranslator;

public interface OntopTranslationFactory {

    QueryTranslator create(OBDASpecification obdaSpecification, ExecutorRegistry executorRegistry);
}