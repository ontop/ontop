package it.unibz.inf.ontop.answering.logging;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.spec.ontology.InconsistentOntologyException;

/**
 * Logs for a concrete query
 */
public interface QueryLogger {

    void declareReformulationFinishedAndSerialize(IQ reformulatedQuery, boolean wasCached);

    void declareResultSetUnblockedAndSerialize();

    void declareLastResultRetrievedAndSerialize(long rowCount);

    void declareReformulationException(OntopReformulationException e);

    void declareEvaluationException(Exception e);

    void declareConnectionException(Exception e);

    void declareConversionException(InconsistentOntologyException e);

    void setSparqlQuery(String sparqlQuery);

    void setSparqlIQ(IQ sparqlIQ);

    void setPlannedQuery(IQ plannedQuery);

    void setPredefinedQuery(String queryId, ImmutableMap<String, String> bindings);

    interface Factory {
        QueryLogger create(ImmutableMultimap<String, String> httpHeaders);
    }
}
