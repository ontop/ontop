package it.unibz.inf.ontop.answering.logging.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;

import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class QueryLoggerImpl implements QueryLogger {
    private final UUID queryId;
    private final long creationTime;
    private final PrintStream outputStream;
    private final OntopReformulationSettings settings;
    private final boolean disabled;
    private final String applicationName;
    private long reformulationTime;
    private long unblockedResulSetTime;
    private static final DateFormat  DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    @Inject
    protected QueryLoggerImpl(OntopReformulationSettings settings) {
        this(System.out, settings);
    }

    protected QueryLoggerImpl(PrintStream outputStream, OntopReformulationSettings settings) {
        this.disabled = !settings.isQueryLoggingEnabled();
        this.outputStream = outputStream;
        this.settings = settings;
        this.queryId = UUID.randomUUID();
        creationTime = System.currentTimeMillis();
        applicationName = settings.getApplicationName();
        reformulationTime = -1;
        unblockedResulSetTime = -1;
    }

    @Override
    public void declareReformulationFinishedAndSerialize(boolean wasCached) {
        if (disabled)
            return;

        reformulationTime = System.currentTimeMillis();
        // TODO: use a proper framework
        String json = String.format(
                "{\"@timestamp\": \"%s\", \"queryId\": \"%s\", \"message\": \"Query reformulated\", \"application\": \"%s\", \"reformulationDuration\": %d, \"reformulationCacheHit\": %b}",
                serializeTimestamp(reformulationTime),
                queryId,
                applicationName,
                reformulationTime - creationTime,
                wasCached);
        outputStream.println(json);
    }

    @Override
    public void declareResultSetUnblockedAndSerialize() {
        if (disabled)
            return;
        unblockedResulSetTime = System.currentTimeMillis();
        if (reformulationTime == -1)
            throw new IllegalStateException("Reformulation should have been declared as finished");

        // TODO: use a proper framework
        String json = String.format(
                "{\"@timestamp\": \"%s\", \"queryId\": \"%s\", \"message\": \"Result set unblocked\", \"application\": \"%s\", \"executionBeforeUnblockingDuration\": %d}",
                serializeTimestamp(unblockedResulSetTime),
                queryId,
                applicationName,
                unblockedResulSetTime - reformulationTime);
        outputStream.println(json);
    }

    @Override
    public void declareLastResultRetrievedAndSerialize(long resultCount) {
        if (disabled)
            return;

        long lastResultFetchedTime = System.currentTimeMillis();
        if (unblockedResulSetTime == -1)
            throw new IllegalStateException("Result set should have been declared as unblocked");

        // TODO: use a proper framework
        String json = String.format(
                "{\"@timestamp\": \"%s\", \"queryId\": \"%s\", \"message\": \"Last result fetched\", \"application\": \"%s\", \"executionAndFetchingDuration\": %d, \"totalDuration\": %d, \"resultCount\": %d}",
                serializeTimestamp(lastResultFetchedTime),
                queryId,
                applicationName,
                lastResultFetchedTime - reformulationTime,
                lastResultFetchedTime - creationTime,
                resultCount);
        outputStream.println(json);
    }

    protected String serializeTimestamp(long time) {
        return DATE_FORMAT.format(new Timestamp(time));
    }
}
