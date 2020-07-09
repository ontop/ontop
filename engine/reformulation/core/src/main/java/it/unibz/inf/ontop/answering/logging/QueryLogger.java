package it.unibz.inf.ontop.answering.logging;

import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.spec.ontology.InconsistentOntologyException;

/**
 * Logs for a concrete query
 */
public interface QueryLogger {

    void declareReformulationFinishedAndSerialize(boolean wasCached);

    void declareResultSetUnblockedAndSerialize();

    void declareLastResultRetrievedAndSerialize(long rowCount);

    void declareReformulationException(OntopReformulationException e);

    void declareEvaluationException(Exception e);

    void declareConnectionException(Exception e);

    void declareConversionException(InconsistentOntologyException e);

    interface Factory {
        QueryLogger create();
    }
}
