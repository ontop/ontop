package it.unibz.inf.ontop.rdf4j.predefined.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.answering.logging.QueryLogger;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.rdf4j.predefined.OntopRDF4JPredefinedQueryEngine;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedQueryConfig.QueryEntry;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import org.apache.http.protocol.HTTP;
import org.eclipse.rdf4j.common.lang.FileFormat;
import org.eclipse.rdf4j.common.lang.service.FileFormatServiceRegistry;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.Query.QueryType;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
import org.eclipse.rdf4j.repository.sparql.federation.CollectionIteration;
import org.eclipse.rdf4j.rio.*;

import java.io.OutputStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static it.unibz.inf.ontop.rdf4j.utils.RDF4JHelper.createStatement;

public class OntopRDF4JPredefinedQueryEngineImpl implements OntopRDF4JPredefinedQueryEngine {


    private final OntopQueryEngine ontopEngine;
    private final ImmutableMap<String, Object> queryMap;
    private final ImmutableMap<String, QueryEntry> queryEntries;

    public OntopRDF4JPredefinedQueryEngineImpl(OntopQueryEngine ontopEngine,
                                               ImmutableMap<String, Object> queryMap,
                                               ImmutableMap<String, QueryEntry> queryEntries) {
        this.ontopEngine = ontopEngine;
        this.queryMap = queryMap;
        this.queryEntries = queryEntries;
    }


    @Override
    public void evaluate(String queryId, ImmutableMap<String, String> bindings,
                         ImmutableList<String> acceptMediaTypes,
                         ImmutableMultimap<String, String> httpHeaders,
                         Consumer<Integer> httpStatusSetter,
                         BiConsumer<String, String> httpHeaderSetter,
                         OutputStream outputStream) throws QueryEvaluationException, RDFHandlerException {

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
                evaluateGraphWithHandler(queryId, bindings, acceptMediaTypes, httpHeaders, httpHeaderSetter, httpStatusSetter, outputStream);
            case TUPLE:
                evaluateTupleWithHandler(queryId, bindings, acceptMediaTypes, httpHeaders, httpHeaderSetter, httpStatusSetter, outputStream);
            default:
                throw new MinorOntopInternalBugException("Unexpected query type");
        }
    }

    private void evaluateGraphWithHandler(String queryId, ImmutableMap<String, String> bindings, ImmutableList<String> acceptMediaTypes,
                                          ImmutableMultimap<String, String> httpHeaders, BiConsumer<String, String> httpHeaderSetter,
                                          Consumer<Integer> httpStatusSetter, OutputStream outputStream) {

        RDFWriterRegistry registry = RDFWriterRegistry.getInstance();
        Optional<RDFFormat> optionalFormat = extractFormat(acceptMediaTypes, registry, RDFFormat.JSONLD,
                m -> Optional.of(m)
                        .filter(a -> a.contains("json"))
                        .map(a -> RDFFormat.JSONLD));

        if (!optionalFormat.isPresent()) {
            // 406: Not acceptable
            httpStatusSetter.accept(406);
            return;
        }

        RDFFormat rdfFormat = optionalFormat.get();
        httpHeaderSetter.accept(HTTP.CONTENT_TYPE, rdfFormat.getDefaultMIMEType() + ";charset=UTF-8");

        // TODO: caching headers (on a per queryId basis)

        RDFWriterFactory rdfWriterFactory = getJsonLdFrame(queryId)
                .filter(f -> rdfFormat.equals(RDFFormat.JSONLD))
                .map(this::createJSONLDFrameWriterFactory)
                .orElseGet(() -> registry.get(rdfFormat)
                        .orElseThrow(() -> new MinorOntopInternalBugException(
                                "The selected RDF format should have a writer factory")));

        RDFWriter handler = rdfWriterFactory.getWriter(outputStream);
        try(GraphQueryResult result =  evaluateGraph(queryId, bindings, httpHeaders)) {
            handler.startRDF();
            while (result.hasNext())
                handler.handleStatement(result.next());
            handler.endRDF();
        }
    }

    private RDFWriterFactory createJSONLDFrameWriterFactory(String jsonLdFrame) {
        throw new RuntimeException("TODO: implement special writer with framing");
    }

    private <FF extends FileFormat, S> Optional<FF> extractFormat(ImmutableList<String> acceptMediaTypes,
                                                                  FileFormatServiceRegistry<FF, S> registry,
                                                                  FF defaultFormat,
                                                                  Function<String, Optional<FF>> otherFormatFct) {
        return acceptMediaTypes.stream()
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
        return Optional.ofNullable(queryEntries.get(queryId))
                .map(QueryEntry::getQueryType);
    }

    /**
     * TODO: implement seriously
     */
    private Optional<String> getJsonLdFrame(String queryId) {
        return Optional.empty();
    }

    @Override
    public GraphQueryResult evaluateGraph(String queryId, ImmutableMap<String, String> bindings) throws QueryEvaluationException {
        if (!getQueryType(queryId)
                .filter(t -> t.equals(QueryType.GRAPH))
                .isPresent())
            throw new IllegalArgumentException("The query" + queryId + " is not defined as a graph query");
        return evaluateGraph(queryId, bindings, ImmutableMultimap.of());
    }

    protected GraphQueryResult evaluateGraph(String queryId, ImmutableMap<String, String> bindings,
                                             ImmutableMultimap<String, String> httpHeaders) throws QueryEvaluationException {


        ConstructTemplate constructTemplate = getConstructTemplate(queryId);
        IQ executableQuery = createExecutableQuery(queryId, bindings);
        QueryLogger queryLogger = createQueryLogger(queryId, bindings, httpHeaders);
        return executeConstructQuery(constructTemplate, executableQuery, queryLogger);
    }

    private ConstructTemplate getConstructTemplate(String queryId) {
        throw new RuntimeException("TODO: extract in advance construct template");
    }

    private IQ createExecutableQuery(String queryId, ImmutableMap<String, String> bindings) {
        throw new RuntimeException("TODO: create executable query");
    }

    private QueryLogger createQueryLogger(String queryId, ImmutableMap<String, String> bindings,
                                          ImmutableMultimap<String, String> httpHeaders) {
        throw new RuntimeException("TODO: create query logger");
    }

    private GraphQueryResult executeConstructQuery(ConstructTemplate constructTemplate, IQ executableQuery,
                                                   QueryLogger queryLogger) {
        try (
                OntopConnection conn = ontopEngine.getConnection();
                OntopStatement stm = conn.createStatement();
                SimpleGraphResultSet res = stm.executeConstructQuery(constructTemplate, executableQuery, queryLogger)
        ){
            // TODO: see how to use stream besides the presence of exception
            ImmutableList.Builder<Statement> resultBuilder = ImmutableList.builder();
            if (res != null) {
                while (res.hasNext()) {
                    RDFFact as = res.next();
                    Statement st = createStatement(as);
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
