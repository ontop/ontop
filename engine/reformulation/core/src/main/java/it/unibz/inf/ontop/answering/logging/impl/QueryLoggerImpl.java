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
        // TODO: get timestamp
        creationTime = System.currentTimeMillis();
    }

    @Override
    public UUID getQueryId() {
        return queryId;
    }

    /**
     * TODO: implement it seriously!
     */
    @Override
    public void declareReformulationFinishedAndSerialize(boolean wasCached) {
        reformulationTime = System.currentTimeMillis();
        // TODO: use a proper framework
        String json = String.format(
                "{\"queryId\": %s, \"reformulationDuration\": %d, \"reformulationCache\": %s}",
                queryId,
                reformulationTime - creationTime,
                wasCached ? "HIT" : "MISS");
        outputStream.println(json);
    }

    /**
     * TODO: implement it seriously!
     */
    @Override
    public void declareResultSetUnblockedAndSerialize() {
        unblockedResulSetTime = System.currentTimeMillis();
        // TODO: use a proper framework
        String json = String.format(
                "{\"queryId\": %s, \"executionBeforeUnblockingDuration\": %d}",
                queryId,
                unblockedResulSetTime - reformulationTime);
        outputStream.println(json);
    }
}
