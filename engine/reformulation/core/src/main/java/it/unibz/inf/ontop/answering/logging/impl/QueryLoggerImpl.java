package it.unibz.inf.ontop.answering.logging.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.logging.impl.ClassAndPropertyExtractor.ClassesAndProperties;
import it.unibz.inf.ontop.answering.logging.impl.QueryTemplateExtractor.QueryTemplateExtraction;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.term.GroundTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.ontology.InconsistentOntologyException;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * See QueryLogger.Factory for constructing new instances
 */
public class QueryLoggerImpl implements QueryLogger {

    public static final String OUTPUT_STREAM_JSON_ERROR = "Problem with the output stream for the query logger. Disabled";
    public static final String EXECUTION_BEFORE_UNBLOCKING_DURATION_KEY = "executionBeforeUnblockingDuration";
    protected static final String REFORMATION_EXC_MSG = "query:exception-reformulation";
    protected static final String EVALUATION_EXC_MSG = "query:exception-evaluation";
    protected static final String CONNECTION_EXC_MSG = "query:exception-connection";
    protected static final String CONVERSION_EXC_MSG = "query:exception-conversion";
    protected static final String SPARQL_QUERY_KEY = "sparqlQuery";
    protected static final String REFORMULATED_QUERY_KEY = "reformulatedQuery";
    protected static final String TIMESTAMP_KEY = "@timestamp";
    protected static final String MESSAGE_KEY = "message";
    protected static final String QUERY_ID_KEY = "queryId";
    protected static final String APPLICATION_KEY = "application";
    protected static final String PAYLOAD_KEY = "payload";
    public static final String QUERY_RESULT_SET_UNBLOCKED = "query:result-set-unblocked";
    public static final String QUERY_LAST_RESULT_FETCHED = "query:last-result-fetched";
    public static final String MERGED_MSG = "query:all";
    public static final String EXECUTION_AND_FETCHING_DURATION_KEY = "executionAndFetchingDuration";
    public static final String RESULT_COUNT_KEY = "resultCount";
    public static final String TOTAL_DURATION_KEY = "totalDuration";
    public static final String EXCEPTION_KEY = "exception";
    public static final String REFORMULATION_DURATION_KEY = "reformulationDuration";
    public static final String REFORMULATION_CACHE_HIT_KEY = "reformulationCacheHit";
    public static final String QUERY_REFORMULATED = "query:reformulated";

    protected static final String CLASSES_KEY = "classesUsedInQuery";
    protected static final String PROPERTIES_KEY = "propertiesUsedInQuery";
    protected static final String TABLES_KEY = "tables";
    protected static final String HTTP_HEADERS_KEY = "httpHeaders";
    protected static final String QUERY_TEMPLATE_KEY = "extractedQueryTemplate";
    protected static final String HASH_KEY = "hash";
    protected static final String PARAMETERS_KEY = "parameters";
    protected static final String PREDEFINED_KEY = "predefined";
    protected static final String PREDEFINED_QUERY_KEY = "queryId";
    protected static final String BINDINGS_KEY = "bindings";

    private static final DateFormat  DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final Logger REGULAR_LOGGER = LoggerFactory.getLogger(QueryLoggerImpl.class);
    ;


    private final UUID queryId;
    private final long creationTime;
    private final PrintStream outputStream;
    private final ImmutableMultimap<String, String> httpHeaders;
    private final OntopReformulationSettings settings;
    private final boolean disabled;
    private final String applicationName;
    private final JsonFactory jsonFactory;
    private final boolean isDecompositionEnabled;
    private final boolean isMergingEnabled;
    private long reformulationTime;
    private long unblockedResulSetTime;
    private final ClassAndPropertyExtractor classAndPropertyExtractor;
    private final RelationNameExtractor relationNameExtractor;
    private final QueryTemplateExtractor queryTemplateExtractor;

    @Nullable
    private ImmutableSet<IRI> classes, properties;
    @Nullable
    private ImmutableSet<String> relationNames;

    @Nullable
    private String sparqlQueryString;

    @Nullable
    private QueryTemplateExtraction queryTemplate;

    @Nullable
    private IQ reformulatedQuery;
    @Nullable
    private Boolean wasReformulationCached;

    @Nullable
    private String predefinedQueryId;

    @Nullable
    private ImmutableMap<String, String> bindings;

    @AssistedInject
    protected QueryLoggerImpl(@Assisted ImmutableMultimap<String, String> httpHeaders,
                              OntopReformulationSettings settings,
                              ClassAndPropertyExtractor classAndPropertyExtractor,
                              RelationNameExtractor relationNameExtractor,
                              QueryTemplateExtractor queryTemplateExtractor) {
        this(System.out, httpHeaders, settings, classAndPropertyExtractor, relationNameExtractor, queryTemplateExtractor);
    }

    protected QueryLoggerImpl(PrintStream outputStream, ImmutableMultimap<String, String> httpHeaders,
                              OntopReformulationSettings settings,
                              ClassAndPropertyExtractor classAndPropertyExtractor,
                              RelationNameExtractor relationNameExtractor, QueryTemplateExtractor queryTemplateExtractor) {
        this.outputStream = outputStream;
        this.httpHeaders = httpHeaders;
        this.settings = settings;
        this.classAndPropertyExtractor = classAndPropertyExtractor;
        this.relationNameExtractor = relationNameExtractor;
        this.queryTemplateExtractor = queryTemplateExtractor;
        this.queryId = UUID.randomUUID();
        creationTime = System.currentTimeMillis();
        applicationName = settings.getApplicationName();
        reformulationTime = -1;
        unblockedResulSetTime = -1;
        jsonFactory = new JsonFactory();

        this.disabled = !settings.isQueryLoggingEnabled();
        this.isDecompositionEnabled = settings.isQueryLoggingDecompositionEnabled();
        this.isMergingEnabled = (!isDecompositionEnabled) || (!settings.areQueryLoggingDecompositionAndMergingMutuallyExclusive());
    }

    @Override
    public void declareReformulationFinishedAndSerialize(IQ reformulatedQuery, boolean wasCached) {
        if (disabled)
            return;

        reformulationTime = System.currentTimeMillis();

        if (isDecompositionEnabled) {
            StringWriter stringWriter = new StringWriter();
            try (JsonGenerator js = jsonFactory.createGenerator(stringWriter)) {
                js.writeStartObject();
                js.writeStringField(TIMESTAMP_KEY, serializeTimestamp(System.currentTimeMillis()));
                js.writeStringField(MESSAGE_KEY, QUERY_REFORMULATED);
                js.writeStringField(APPLICATION_KEY, applicationName);
                js.writeObjectFieldStart(PAYLOAD_KEY);
                js.writeStringField(QUERY_ID_KEY, queryId.toString());
                writeReformulationSpecificFields(reformulatedQuery, wasCached, js);
                js.writeEndObject();
                js.writeEndObject();
            } catch (IOException ex) {
                REGULAR_LOGGER.error(OUTPUT_STREAM_JSON_ERROR + ex);
            }
            outputStream.println(stringWriter.toString());
        }

        if (isMergingEnabled) {
            this.reformulatedQuery = reformulatedQuery;
            this.wasReformulationCached = wasCached;
        }

    }

    protected void writeReformulationSpecificFields(IQ reformulatedQuery, boolean wasCached, JsonGenerator js) throws IOException {
        // Classes
        if (classes != null) {
            js.writeArrayFieldStart(CLASSES_KEY);
            for (IRI klass : classes)
                js.writeString(klass.getIRIString());
            js.writeEndArray();
        }
        // Properties
        if (properties != null) {
            js.writeArrayFieldStart(PROPERTIES_KEY);
            for (IRI p : properties)
                js.writeString(p.getIRIString());
            js.writeEndArray();
        }
        // Relations
        if (relationNames != null) {
            js.writeArrayFieldStart(TABLES_KEY);
            for (String n : relationNames)
                js.writeString(n);
            js.writeEndArray();
        }
        js.writeNumberField(REFORMULATION_DURATION_KEY, reformulationTime - creationTime);
        js.writeBooleanField(REFORMULATION_CACHE_HIT_KEY, wasCached);

        writeHttpHeaders(js);
        writeQueryTemplateExtraction(js);
        writePredefinedQueryInfo(js);

        if (sparqlQueryString != null)
            js.writeStringField(SPARQL_QUERY_KEY, sparqlQueryString);
        if (settings.isReformulatedQueryIncludedIntoQueryLog())
            js.writeStringField(REFORMULATED_QUERY_KEY, reformulatedQuery.toString());
    }

    private void writeHttpHeaders(JsonGenerator js) throws IOException {
        js.writeObjectFieldStart(HTTP_HEADERS_KEY);
        ImmutableSet<String> namesToLog = settings.getHttpHeaderNamesToLog();

        for (Map.Entry<String, Collection<String>> e : httpHeaders.asMap().entrySet()) {
            String normalizedKey = e.getKey().toLowerCase();
            if (namesToLog.contains(normalizedKey)) {
                // We only consider the first value
                js.writeStringField(normalizedKey, e.getValue().iterator().next());
            }
        }
        js.writeEndObject();
    }

    private void writeQueryTemplateExtraction(JsonGenerator js) throws IOException {
        if (queryTemplate == null)
            return;
        js.writeObjectFieldStart(QUERY_TEMPLATE_KEY);

        // TODO: update Guava
        @SuppressWarnings("UnstableApiUsage")
        String iqHash = Hashing.sha256()
                .hashString(queryTemplate.getIq().toString(), StandardCharsets.UTF_8)
                .toString();
        js.writeStringField(HASH_KEY, iqHash);

        js.writeObjectFieldStart(PARAMETERS_KEY);
        for (Map.Entry<GroundTerm, Variable> e : queryTemplate.getParameterMap().entrySet()) {
            js.writeStringField(e.getValue().toString(), e.getKey().toString());
        }
        js.writeEndObject();
        js.writeEndObject();
    }

    private void writePredefinedQueryInfo(JsonGenerator js) throws IOException {
        if (predefinedQueryId == null || bindings == null)
            return;
        js.writeObjectFieldStart(PREDEFINED_KEY);

        js.writeStringField(PREDEFINED_QUERY_KEY, predefinedQueryId);

        js.writeObjectFieldStart(BINDINGS_KEY);
        for (Map.Entry<String, String> e : bindings.entrySet()) {
            js.writeStringField(e.getKey(), e.getValue());
        }
        js.writeEndObject();
        js.writeEndObject();

    }

    @Override
    public void declareResultSetUnblockedAndSerialize() {
        if (disabled)
            return;
        unblockedResulSetTime = System.currentTimeMillis();

        if (isDecompositionEnabled) {

            StringWriter stringWriter = new StringWriter();
            try (JsonGenerator js = jsonFactory.createGenerator(stringWriter)) {
                js.writeStartObject();
                js.writeStringField(TIMESTAMP_KEY, serializeTimestamp(unblockedResulSetTime));
                js.writeStringField(MESSAGE_KEY, QUERY_RESULT_SET_UNBLOCKED);
                js.writeStringField(APPLICATION_KEY, applicationName);
                js.writeObjectFieldStart(PAYLOAD_KEY);
                js.writeStringField(QUERY_ID_KEY, queryId.toString());
                writeResultSetUnblockedSpecificFields(js);
                js.writeEndObject();
                js.writeEndObject();
            } catch (IOException e) {
                REGULAR_LOGGER.error(OUTPUT_STREAM_JSON_ERROR + e);
                return;
            }
            outputStream.println(stringWriter.toString());
        }
    }

    protected void writeResultSetUnblockedSpecificFields(JsonGenerator js) throws IOException {
        // For DESCRIBE, reformulation time is not provided
        if (reformulationTime != -1)
            js.writeNumberField(EXECUTION_BEFORE_UNBLOCKING_DURATION_KEY, unblockedResulSetTime - reformulationTime);
    }

    @Override
    public void declareLastResultRetrievedAndSerialize(long resultCount) {
        if (disabled)
            return;

        long lastResultFetchedTime = System.currentTimeMillis();
        if (unblockedResulSetTime == -1)
            throw new IllegalStateException("Result set should have been declared as unblocked");

        if (isDecompositionEnabled) {

            StringWriter stringWriter = new StringWriter();
            try (JsonGenerator js = jsonFactory.createGenerator(stringWriter)) {
                js.writeStartObject();
                js.writeStringField(TIMESTAMP_KEY, serializeTimestamp(lastResultFetchedTime));
                js.writeStringField(MESSAGE_KEY, QUERY_LAST_RESULT_FETCHED);
                js.writeStringField(APPLICATION_KEY, applicationName);
                js.writeObjectFieldStart(PAYLOAD_KEY);
                js.writeStringField(QUERY_ID_KEY, queryId.toString());
                writeLastResultRetrievedSpecificFields(js, lastResultFetchedTime, resultCount);
                js.writeEndObject();
                js.writeEndObject();
            } catch (IOException e) {
                REGULAR_LOGGER.error(OUTPUT_STREAM_JSON_ERROR + e);
            }
            outputStream.println(stringWriter.toString());
        }

        if (isMergingEnabled) {
            serializeMergedMessage(lastResultFetchedTime, resultCount);
        }
    }

    protected void writeLastResultRetrievedSpecificFields(JsonGenerator js, long lastResultFetchedTime, long resultCount) throws IOException {
        // For DESCRIBE, reformulation time is not provided
        if (reformulationTime != -1)
            js.writeNumberField(EXECUTION_AND_FETCHING_DURATION_KEY, lastResultFetchedTime - reformulationTime);
        js.writeNumberField(TOTAL_DURATION_KEY, lastResultFetchedTime - creationTime);
        js.writeNumberField(RESULT_COUNT_KEY, resultCount);
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
        if (disabled)
            return;

        if(settings.areClassesAndPropertiesIncludedIntoQueryLog()) {
            ClassesAndProperties classesAndProperties = classAndPropertyExtractor.extractClassesAndProperties(sparqlIQ);
            classes = classesAndProperties.getClasses();
            properties = classesAndProperties.getProperties();
        }

        if (settings.isQueryTemplateExtractionEnabled()) {
            Optional<QueryTemplateExtraction> extraction = queryTemplateExtractor.extract(sparqlIQ);

            extraction.ifPresent(e -> queryTemplate = e);
        }
    }

    @Override
    public void setPlannedQuery(IQ plannedQuery) {
        if (disabled || (!settings.areTablesIncludedIntoQueryLog()))
            return;

        relationNames = relationNameExtractor.extractRelationNames(plannedQuery);
    }

    @Override
    public void setPredefinedQuery(String queryId, ImmutableMap<String, String> bindings) {
        if (disabled)
            return;

        predefinedQueryId = queryId;
        this.bindings = bindings;

    }

    protected void declareException(Exception e, String exceptionType) {
        if (disabled)
            return;

        StringWriter stringWriter = new StringWriter();
        try (JsonGenerator js = jsonFactory.createGenerator(stringWriter)) {
            js.writeStartObject();
            js.writeStringField(TIMESTAMP_KEY, serializeTimestamp(System.currentTimeMillis()));
            js.writeStringField(MESSAGE_KEY, exceptionType);
            js.writeStringField(APPLICATION_KEY, applicationName);
            js.writeObjectFieldStart(PAYLOAD_KEY);
            js.writeStringField(QUERY_ID_KEY, queryId.toString());
            js.writeStringField(EXCEPTION_KEY, e.getMessage());
            if (sparqlQueryString != null)
                js.writeStringField(SPARQL_QUERY_KEY, sparqlQueryString);
            if (reformulatedQuery != null)
                js.writeStringField(REFORMULATED_QUERY_KEY, reformulatedQuery.toString());
            js.writeEndObject();
            js.writeEndObject();
        } catch (IOException ex) {
            REGULAR_LOGGER.error(OUTPUT_STREAM_JSON_ERROR + ex);
        }
        outputStream.println(stringWriter.toString());
    }

    protected String serializeTimestamp(long time) {
        return DATE_FORMAT.format(new Timestamp(time));
    }

    /**
     * Optional summary message
     */
    protected void serializeMergedMessage(long lastResultFetchedTime, long resultCount) {
        StringWriter stringWriter = new StringWriter();
        try (JsonGenerator js = jsonFactory.createGenerator(stringWriter)) {
            js.writeStartObject();
            js.writeStringField(TIMESTAMP_KEY, serializeTimestamp(lastResultFetchedTime));
            js.writeStringField(MESSAGE_KEY, MERGED_MSG);
            js.writeStringField(APPLICATION_KEY, applicationName);
            js.writeObjectFieldStart(PAYLOAD_KEY);
            js.writeStringField(QUERY_ID_KEY, queryId.toString());
            if (reformulatedQuery != null)
                //noinspection ConstantConditions
                writeReformulationSpecificFields(reformulatedQuery, wasReformulationCached, js);
            writeResultSetUnblockedSpecificFields(js);
            writeLastResultRetrievedSpecificFields(js, lastResultFetchedTime, resultCount);
            js.writeEndObject();
            js.writeEndObject();
        } catch (IOException e) {
            REGULAR_LOGGER.error(OUTPUT_STREAM_JSON_ERROR + e);
        }
        outputStream.println(stringWriter.toString());
    }
}
