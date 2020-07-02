package it.unibz.inf.ontop.answering.logging;

/**
 * Logs for a concrete query
 */
public interface QueryLogger {

    void declareReformulationFinishedAndSerialize(boolean wasCached);

    void declareResultSetUnblockedAndSerialize();

    void declareLastResultRetrievedAndSerialize(long rowCount);

    interface Factory {
        QueryLogger create();
    }
}
