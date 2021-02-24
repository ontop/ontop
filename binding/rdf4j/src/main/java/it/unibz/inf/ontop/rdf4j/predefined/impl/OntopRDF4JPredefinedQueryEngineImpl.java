package it.unibz.inf.ontop.rdf4j.predefined.impl;

import static it.unibz.inf.ontop.rdf4j.utils.RDF4JHelper.createStatement;

import com.github.jsonldjava.core.DocumentLoader;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.QueryReformulator;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQuery;
import it.unibz.inf.ontop.answering.resultset.GraphResultSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopReformulationException;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.rdf4j.jsonld.EmptyResultException;
import it.unibz.inf.ontop.rdf4j.jsonld.FramedJSONLDWriterFactory;
import it.unibz.inf.ontop.rdf4j.predefined.*;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import org.apache.http.protocol.HTTP;
import org.eclipse.rdf4j.common.lang.FileFormat;
import org.eclipse.rdf4j.common.lang.service.FileFormatServiceRegistry;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.Query.QueryType;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
import org.eclipse.rdf4j.repository.sparql.federation.CollectionIteration;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.JSONLDMode;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class OntopRDF4JPredefinedQueryEngineImpl implements OntopRDF4JPredefinedQueryEngine {

    private final OntopQueryEngine ontopEngine;
    private final ImmutableMap<String, PredefinedGraphQuery> graphQueries;
    private final ImmutableMap<String, PredefinedTupleQuery> tupleQueries;
    private final QueryReformulator queryReformulator;
    private final QueryLogger.Factory queryLoggerFactory;
    private final Cache<ImmutableMap<String, String>, IQ> referenceQueryCache;
    private final ReferenceValueReplacer valueReplacer;
    private final DocumentLoader documentLoader;

    private static final Logger LOGGER = LoggerFactory.getLogger(OntopRDF4JPredefinedQueryEngineImpl.class);
    private final SecureRandom random;

    public OntopRDF4JPredefinedQueryEngineImpl(OntopQueryEngine ontopEngine,
                                               PredefinedQueries predefinedQueries,
                                               OntopSystemConfiguration configuration) {
        this.ontopEngine = ontopEngine;
        this.graphQueries = predefinedQueries.getGraphQueries();
        this.tupleQueries = predefinedQueries.getTupleQueries();
        this.queryReformulator = ontopEngine.getQueryReformulator();

        Injector injector = configuration.getInjector();
        queryLoggerFactory = injector.getInstance(QueryLogger.Factory.class);
        valueReplacer = injector.getInstance(ReferenceValueReplacer.class);

        // TODO: think about the dimensions
        referenceQueryCache = CacheBuilder.newBuilder()
                .build();

        documentLoader = new DocumentLoader();
        predefinedQueries.getContextMap().forEach(
                (k,m) -> {
                    try {
                        documentLoader.addInjectedDoc(k, JsonUtils.toString(m));
                    } catch (IOException e) {
                        throw new MinorOntopInternalBugException("Unexpected issue when serialize a parsed JSON element");
                    }
                });

        random = new SecureRandom();
    }


    @Override
    public void evaluate(String queryId, ImmutableMap<String, String> bindings,
                         ImmutableList<String> acceptMediaTypes,
                         ImmutableMultimap<String, String> httpHeaders,
                         Consumer<Integer> httpStatusSetter,
                         BiConsumer<String, String> httpHeaderSetter,
                         OutputStream outputStream) throws LateEvaluationOrConversionException {

        Optional<QueryType> optionalQueryType = getQueryType(queryId);
        if (!optionalQueryType.isPresent()) {
            // 404: Not found
            httpStatusSetter.accept(404);
            return;
        }

        switch (optionalQueryType.get()) {
            case BOOLEAN:
                throw new RuntimeException("TODO: support ask queries");
            case GRAPH:
                evaluateGraphWithHandler(graphQueries.get(queryId), bindings, acceptMediaTypes, httpHeaders, httpHeaderSetter, httpStatusSetter, outputStream);
                break;
            case TUPLE:
                evaluateTupleWithHandler(queryId, bindings, acceptMediaTypes, httpHeaders, httpHeaderSetter, httpStatusSetter, outputStream);
                break;
            default:
                throw new MinorOntopInternalBugException("Unexpected query type");
        }
    }

    @Override
    public String evaluate(String queryId, ImmutableMap<String, String> bindings, ImmutableList<String> acceptMediaTypes,
                           ImmutableMultimap<String, String> httpHeaders, Consumer<Integer> httpStatusSetter,
                           BiConsumer<String, String> httpHeaderSetter) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            evaluate(queryId, bindings, acceptMediaTypes, httpHeaders, httpStatusSetter, httpHeaderSetter, outputStream);
            return outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (LateEvaluationOrConversionException e) {
            httpStatusSetter.accept(500);
            return e.getMessage();
        } catch (UnsupportedEncodingException e) {
            throw new MinorOntopInternalBugException("UTF-8 was expected to be supported");
        }
    }

    /**
     * Disabled by default
     */
    @Override
    public boolean shouldStream(String queryId) {
        if (graphQueries.containsKey(queryId))
            return graphQueries.get(queryId)
                    .isResultStreamingEnabled();
        else if (tupleQueries.containsKey(queryId))
            return tupleQueries.get(queryId)
                    .isResultStreamingEnabled();
        else
            return false;
    }

    private void evaluateGraphWithHandler(PredefinedGraphQuery predefinedQuery, ImmutableMap<String, String> bindings, ImmutableList<String> acceptMediaTypes,
                                          ImmutableMultimap<String, String> httpHeaders, BiConsumer<String, String> httpHeaderSetter,
                                          Consumer<Integer> httpStatusSetter, OutputStream outputStream) throws LateEvaluationOrConversionException {

        RDFWriterRegistry registry = RDFWriterRegistry.getInstance();

        Stream<String> mediaTypes = acceptMediaTypes.stream()
                // So as to give priority to JSON-LD in browser
                .filter(s -> !s.equals("application/xml"));

        Optional<RDFFormat> optionalFormat = extractFormat(mediaTypes, registry, RDFFormat.JSONLD,
                m -> Optional.of(m)
                        .filter(a -> a.contains("json"))
                        .map(a -> RDFFormat.JSONLD));

        if (!optionalFormat.isPresent()) {
            // 406: Not acceptable
            httpStatusSetter.accept(406);
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.println("Not acceptable. Suggested format: application/ld+json .");
            printWriter.flush();
            return;
        }

        RDFFormat rdfFormat = optionalFormat.get();

        // TODO: caching headers (on a per queryId basis)

        RDFWriterFactory rdfWriterFactory = predefinedQuery.getJsonLdFrame()
                .filter(f -> rdfFormat.equals(RDFFormat.JSONLD))
                .map(jsonLdFrame -> createJSONLDFrameWriterFactory(jsonLdFrame, predefinedQuery.shouldReturn404IfEmpty()))
                .orElseGet(() -> registry.get(rdfFormat)
                        .orElseThrow(() -> new MinorOntopInternalBugException(
                                "The selected RDF format should have a writer factory")));

        QueryLogger queryLogger = createQueryLogger(predefinedQuery, bindings, httpHeaders);
        try {
            IQ executableQuery = createExecutableQuery(predefinedQuery, bindings, queryLogger);

            httpHeaderSetter.accept(HTTP.CONTENT_TYPE, rdfFormat.getDefaultMIMEType() + ";charset=UTF-8");

            RDFWriter handler = rdfWriterFactory.getWriter(outputStream);
            if (rdfFormat.equals(RDFFormat.JSONLD)) {
                // As of 10/2020, native types are ignored by the default JSON-LD writer of RDF4J
                handler.set(JSONLDSettings.USE_NATIVE_TYPES, true);
                handler.set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
            }

            try (GraphQueryResult result = executeConstructQuery(predefinedQuery.getConstructTemplate(), executableQuery, queryLogger)) {
                handler.startRDF();
                while (result.hasNext())
                    handler.handleStatement(result.next());
                handler.endRDF();
            }
        } catch (InvalidBindingSetException e) {
            httpStatusSetter.accept(400);
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.println(e.getMessage());
            printWriter.flush();

        } catch (OntopReformulationException e) {
            httpStatusSetter.accept(500);
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.println("Problem reformulating the underlying SPARQL query: \n" + e.getMessage());
            printWriter.flush();
        } catch (EmptyResultException e) {
            httpStatusSetter.accept(404);
            httpHeaderSetter.accept(HTTP.CONTENT_TYPE, "text/plain");
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.println(String.format("No result for %s with the parameters %s",
                    predefinedQuery.getId(), bindings));
            printWriter.flush();
        } catch (QueryEvaluationException | RDFHandlerException e) {
            throw new LateEvaluationOrConversionException(e);
        }
    }

    private RDFWriterFactory createJSONLDFrameWriterFactory(Map<String, Object> jsonLdFrame,
                                                            boolean throwExceptionIfEmpty) {
        return new FramedJSONLDWriterFactory(jsonLdFrame, documentLoader, throwExceptionIfEmpty);
    }

    private <FF extends FileFormat, S> Optional<FF> extractFormat(Stream<String> acceptMediaTypes,
                                                                  FileFormatServiceRegistry<FF, S> registry,
                                                                  FF defaultFormat,
                                                                  Function<String, Optional<FF>> otherFormatFct) {
        return acceptMediaTypes
                .map(m -> m.startsWith("*/*")
                        ? Optional.of(defaultFormat)
                        : registry.getFileFormatForMIMEType(m)
                        .map(Optional::of)
                        .orElseGet(() -> otherFormatFct.apply(m)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(f -> registry.get(f).isPresent())
                .findFirst();
    }

    private Optional<QueryType> getQueryType(String queryId) {
        if (graphQueries.containsKey(queryId))
            return Optional.of(QueryType.GRAPH);
        else if (tupleQueries.containsKey(queryId))
            return Optional.of(QueryType.TUPLE);
        else
            return Optional.empty();
    }

    @Override
    public GraphQueryResult evaluateGraph(String queryId, ImmutableMap<String, String> bindings) throws QueryEvaluationException {

        PredefinedGraphQuery predefinedQuery = Optional.ofNullable(graphQueries.get(queryId))
                .orElseThrow(() -> new IllegalArgumentException("The query" + queryId + " is not defined as a graph query"));
        ConstructTemplate constructTemplate = predefinedQuery.getConstructTemplate();
        QueryLogger queryLogger = createQueryLogger(predefinedQuery, bindings, ImmutableMultimap.of());
        try {
            IQ executableQuery = createExecutableQuery(predefinedQuery, bindings, queryLogger);
            return executeConstructQuery(constructTemplate, executableQuery, queryLogger);
        } catch (OntopReformulationException | InvalidBindingSetException e) {
            throw new QueryEvaluationException(e);
        }
    }

    private IQ createExecutableQuery(PredefinedQuery predefinedQuery, ImmutableMap<String, String> bindings,
                                     QueryLogger queryLogger) throws OntopReformulationException, InvalidBindingSetException {

        // May throw an InvalidBindingSetException
        predefinedQuery.validate(bindings);

        ImmutableMap<String, String> bindingWithReferences = predefinedQuery.replaceWithReferenceValues(bindings);

        IQ existingReferenceIQ = referenceQueryCache.getIfPresent(bindingWithReferences);
        // NB: no problem if concurrent reference queries are generated (deterministic results)
        IQ referenceIQ = existingReferenceIQ == null
                ? generateReferenceQuery(predefinedQuery, bindingWithReferences)
                : existingReferenceIQ;
        if (existingReferenceIQ == null)
            referenceQueryCache.put(bindingWithReferences, referenceIQ);

        IQ newIQ = valueReplacer.replaceReferenceValues(referenceIQ, bindings, bindingWithReferences);

        queryLogger.declareReformulationFinishedAndSerialize(newIQ, referenceIQ == existingReferenceIQ);

        return newIQ;
    }

    private IQ generateReferenceQuery(PredefinedQuery predefinedQuery,
                                      ImmutableMap<String, String> bindingWithReferences)
            throws OntopReformulationException {
        BindingSet bindingSet = predefinedQuery.convertBindings(bindingWithReferences);
        RDF4JInputQuery newQuery = predefinedQuery.getInputQuery()
                .newBindings(bindingSet);

        // TODO: shall we consider some HTTP headers?
        QueryLogger tmpQueryLogger = queryLoggerFactory.create(ImmutableMultimap.of());

        LOGGER.debug("Generating the reference query for {} with ref parameters {}",
                predefinedQuery.getId(),
                bindingSet);

        return queryReformulator.reformulateIntoNativeQuery(newQuery, tmpQueryLogger);
    }

    private QueryLogger createQueryLogger(PredefinedQuery predefinedQuery, ImmutableMap<String, String> bindings,
                                          ImmutableMultimap<String, String> httpHeaders) {
        QueryLogger queryLogger = queryLoggerFactory.create(httpHeaders);
        queryLogger.setPredefinedQuery(predefinedQuery.getId(), bindings);
        return queryLogger;
    }

    private GraphQueryResult executeConstructQuery(ConstructTemplate constructTemplate, IQ executableQuery,
                                                   QueryLogger queryLogger) {
        try (
                OntopConnection conn = ontopEngine.getConnection();
                OntopStatement stm = conn.createStatement();
                GraphResultSet res = stm.executeConstructQuery(constructTemplate, executableQuery, queryLogger)
        ){
            byte[] salt = new byte[20];
            random.nextBytes(salt);


            // TODO: see how to use stream besides the presence of exceptions
            ImmutableList.Builder<Statement> resultBuilder = ImmutableList.builder();
            if (res != null) {
                while (res.hasNext()) {
                    RDFFact as = res.next();
                    Statement st = createStatement(as, salt);
                    if (st!=null)
                        resultBuilder.add(st);
                }
            }
            return new IteratingGraphQueryResult(ImmutableMap.of(), new CollectionIteration<>(resultBuilder.build()));

        } catch (Exception e) {
            throw new QueryEvaluationException(e);
        }
    }

    private void evaluateTupleWithHandler(String queryId, ImmutableMap<String, String> bindings, ImmutableList<String> acceptMediaTypes,
                                          ImmutableMultimap<String, String> httpHeaders, BiConsumer<String, String> httpHeaderSetter,
                                          Consumer<Integer> httpStatusSetter, OutputStream outputStream) {
        throw new RuntimeException("TODO: support SELECT queries");
    }
}
