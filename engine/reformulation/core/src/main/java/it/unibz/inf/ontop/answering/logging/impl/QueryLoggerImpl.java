package it.unibz.inf.ontop.answering.logging.impl;

import it.unibz.inf.ontop.answering.logging.QueryLogger;

import java.io.PrintStream;
import java.util.UUID;

public class QueryLoggerImpl implements QueryLogger {
    private final UUID queryId;
    private final long creationTime;
    private final PrintStream outputStream;
    private long reformulationTime;
    private long unblockedResulSetTime;

    public QueryLoggerImpl(PrintStream outputStream) {
        this.outputStream = outputStream;
        this.queryId = UUID.randomUUID();
        creationTime = System.currentTimeMillis();
        reformulationTime = -1;
        unblockedResulSetTime = -1;
    }

    /**
     * TODO: implement it seriously!
     */
    @Override
    public void declareReformulationFinishedAndSerialize(boolean wasCached) {
        reformulationTime = System.currentTimeMillis();
        // TODO: use a proper framework
        String json = String.format(
                "{\"queryId\": %s, \"reformulationDuration\": %d, \"reformulationCacheHit\": %b}",
                queryId,
                reformulationTime - creationTime,
                wasCached);
        outputStream.println(json);
    }

    /**
     * TODO: implement it seriously!
     */
    @Override
    public void declareResultSetUnblockedAndSerialize() {
        unblockedResulSetTime = System.currentTimeMillis();
        if (reformulationTime == -1)
            throw new IllegalStateException("Reformulation should have been declared as finished");

        // TODO: use a proper framework
        String json = String.format(
                "{\"queryId\": %s, \"executionBeforeUnblockingDuration\": %d}",
                queryId,
                unblockedResulSetTime - reformulationTime);
        outputStream.println(json);
    }

    /**
     * TODO: implement it seriously!
     * @param resultCount
     */
    @Override
    public void declareLastResultRetrievedAndSerialize(long resultCount) {
        long lastResultFetchedTime = System.currentTimeMillis();
        if (unblockedResulSetTime == -1)
            throw new IllegalStateException("Result set should have been declared as unblocked");

        // TODO: use a proper framework
        String json = String.format(
                "{\"queryId\": %s, \"executionAndFetchingDuration\": %d, \"totalDuration\": %d, \"resultCount\": %d}",
                queryId,
                lastResultFetchedTime - reformulationTime,
                lastResultFetchedTime - creationTime,
                resultCount);
        outputStream.println(json);
    }
}
