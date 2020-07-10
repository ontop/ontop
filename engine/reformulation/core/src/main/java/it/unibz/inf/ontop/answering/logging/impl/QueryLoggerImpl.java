package it.unibz.inf.ontop.answering.logging.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.logging.impl.ClassAndPropertyExtractor.ClassesAndProperties;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.spec.ontology.InconsistentOntologyException;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.stream.Collectors;

public class QueryLoggerImpl implements QueryLogger {

    protected static final String REFORMATION_EXC_MSG = "query:exception-reformulation";
    protected static final String EVALUATION_EXC_MSG = "query:exception-evaluation";
    protected static final String CONNECTION_EXC_MSG = "query:exception-connection";
    protected static final String CONVERSION_EXC_MSG = "query:exception-conversion";
    protected static final String SPARQL_QUERY_KEY = "sparqlQuery";
    protected static final String REFORMULATED_QUERY_KEY = "reformulatedQuery";

    protected static final String CLASSES_KEY = "classes";
    protected static final String PROPERTIES_KEY = "properties";
    protected static final String TABLES_KEY = "tables";

    private static final DateFormat  DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private final UUID queryId;
    private final long creationTime;
    private final PrintStream outputStream;
    private final OntopReformulationSettings settings;
    private final boolean disabled;
    private final String applicationName;
    private long reformulationTime;
    private long unblockedResulSetTime;
    private final ClassAndPropertyExtractor classAndPropertyExtractor;
    private final RelationNameExtractor relationNameExtractor;

    @Nullable
    private ImmutableSet<IRI> classes, properties;
    @Nullable
    private ImmutableSet<String> relationNames;

    @Nullable
    private String sparqlQueryString;

    @Inject
    protected QueryLoggerImpl(OntopReformulationSettings settings,
                              ClassAndPropertyExtractor classAndPropertyExtractor,
                              RelationNameExtractor relationNameExtractor) {
        this(System.out, settings, classAndPropertyExtractor, relationNameExtractor);
    }

    protected QueryLoggerImpl(PrintStream outputStream, OntopReformulationSettings settings,
                              ClassAndPropertyExtractor classAndPropertyExtractor,
                              RelationNameExtractor relationNameExtractor) {
        this.disabled = !settings.isQueryLoggingEnabled();
        this.outputStream = outputStream;
        this.settings = settings;
        this.classAndPropertyExtractor = classAndPropertyExtractor;
        this.relationNameExtractor = relationNameExtractor;
        this.queryId = UUID.randomUUID();
        creationTime = System.currentTimeMillis();
        applicationName = settings.getApplicationName();
        reformulationTime = -1;
        unblockedResulSetTime = -1;
    }

    @Override
    public void declareReformulationFinishedAndSerialize(IQ reformulatedQuery, boolean wasCached) {
        if (disabled)
            return;

        String reformulatedQueryString = settings.isReformulatedQueryIncludedIntoQueryLog()
                ? serializeEntry(REFORMULATED_QUERY_KEY, reformulatedQuery.toString())
                : "";

        reformulationTime = System.currentTimeMillis();
        // TODO: use a proper framework
        String json = String.format(
                "{\"@timestamp\": \"%s\", " +
                        "\"application\": \"%s\", " +
                        "\"message\": \"query:reformulated\", " +
                        "\"payload\": { " +
                        "\"queryId\": \"%s\", " +
                        "%s %s %s %s %s" +
                        "\"reformulationDuration\": %d, " +
                        "\"reformulationCacheHit\": %b } }",
                serializeTimestamp(reformulationTime),
                applicationName,
                queryId,
                serializeEntry(SPARQL_QUERY_KEY, sparqlQueryString),
                reformulatedQueryString,
                serializeArrayEntry(CLASSES_KEY, classes),
                serializeArrayEntry(PROPERTIES_KEY, properties),
                serializeArrayEntry(TABLES_KEY, relationNames),
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
                "{\"@timestamp\": \"%s\", \"application\": \"%s\", \"message\": \"query:result-set-unblocked\", \"payload\": { \"queryId\": \"%s\", \"executionBeforeUnblockingDuration\": %d } }",
                serializeTimestamp(unblockedResulSetTime),
                applicationName,
                queryId,
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
                "{\"@timestamp\": \"%s\", \"application\": \"%s\" \"message\": \"query:last-result-fetched\", \"payload\": { \"queryId\": \"%s\", \"executionAndFetchingDuration\": %d, \"totalDuration\": %d, \"resultCount\": %d } }",
                serializeTimestamp(lastResultFetchedTime),
                applicationName,
                queryId,
                lastResultFetchedTime - reformulationTime,
                lastResultFetchedTime - creationTime,
                resultCount);
        outputStream.println(json);
    }

    @Override
    public void declareReformulationException(OntopReformulationException e) {
        declareException(e, REFORMATION_EXC_MSG);
    }

    @Override
    public void declareEvaluationException(Exception e) {
        declareException(e, EVALUATION_EXC_MSG);
    }

    @Override
    public void declareConnectionException(Exception e) {
        declareException(e, CONNECTION_EXC_MSG);
    }

    @Override
    public void declareConversionException(InconsistentOntologyException e) {
        declareException(e, CONVERSION_EXC_MSG);
    }

    @Override
    public void setSparqlQuery(String sparqlQuery) {
        if (disabled || (!settings.isSparqlQueryIncludedIntoQueryLog()))
            return;

        if (sparqlQueryString != null)
            throw new IllegalStateException("Already specified SPARQL query");
        sparqlQueryString = sparqlQuery;
    }

    @Override
    public void setSparqlIQ(IQ sparqlIQ) {
        if (disabled || (!settings.areClassesAndPropertiesIncludedIntoQueryLog()))
            return;

        ClassesAndProperties classesAndProperties = classAndPropertyExtractor.extractClassesAndProperties(sparqlIQ);
        classes = classesAndProperties.getClasses();
        properties = classesAndProperties.getProperties();
    }

    @Override
    public void setPlannedQuery(IQ plannedQuery) {
        if (disabled || (!settings.areTablesIncludedIntoQueryLog()))
            return;

        relationNames = relationNameExtractor.extractRelationNames(plannedQuery);
    }

    protected void declareException(Exception e, String exceptionType) {
        if (disabled)
            return;

        // TODO: use a proper framework
        String json = String.format(
                "{\"@timestamp\": \"%s\", \"application\": \"%s\" \"message\": \"%s\", \"payload\": { \"queryId\": \"%s\", \"exception\": %s} }",
                serializeTimestamp(System.currentTimeMillis()),
                applicationName,
                exceptionType,
                queryId,
                e.getMessage());
        outputStream.println(json);
    }

    protected String serializeTimestamp(long time) {
        return DATE_FORMAT.format(new Timestamp(time));
    }

    protected String serializeEntry(String key, @Nullable String value) {
        return (value == null)
                ? ""
                : String.format("\"%s\": \"%s\", ", key, escapeDoubleQuotes(value));
    }

    protected String escapeDoubleQuotes(String value) {
        return value.replaceAll("\"", "\\\"");
    }

    protected String serializeArrayEntry(String key, ImmutableSet<? extends Object> arguments) {
        if (arguments == null)
            return "";

        return String.format("[%s]", arguments.stream()
                .map(a -> escapeDoubleQuotes(a.toString()))
                .collect(Collectors.joining(", ")));
    }
}
