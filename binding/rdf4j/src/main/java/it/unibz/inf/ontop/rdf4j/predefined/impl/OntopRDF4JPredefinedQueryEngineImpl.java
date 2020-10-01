package it.unibz.inf.ontop.rdf4j.predefined.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.rdf4j.predefined.OntopRDF4JPredefinedQueryEngine;
import it.unibz.inf.ontop.rdf4j.predefined.PredefinedConfig;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.apache.http.protocol.HTTP;
import org.eclipse.rdf4j.common.lang.FileFormat;
import org.eclipse.rdf4j.common.lang.service.FileFormatServiceRegistry;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.Query.QueryType;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.rio.*;

import java.io.OutputStream;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class OntopRDF4JPredefinedQueryEngineImpl implements OntopRDF4JPredefinedQueryEngine {

    private final OntopVirtualRepository repository;

    public OntopRDF4JPredefinedQueryEngineImpl(OntopVirtualRepository repository,
                                               ImmutableMap<String, Object> queryMap,
                                               ImmutableMap<String, PredefinedConfig.QueryEntry> queries) {
        this.repository = repository;
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
        throw new RuntimeException("TODO: implement it");
    }

    /**
     * TODO: implement seriously
     */
    private Optional<String> getJsonLdFrame(String queryId) {
        return Optional.empty();
    }

    @Override
    public GraphQueryResult evaluateGraph(String queryId, ImmutableMap<String, String> bindings) throws QueryEvaluationException {
        return evaluateGraph(queryId, bindings, ImmutableMultimap.of());
    }

    protected GraphQueryResult evaluateGraph(String queryId, ImmutableMap<String, String> bindings,
                                             ImmutableMultimap<String, String> httpHeaders) throws QueryEvaluationException {
        throw new RuntimeException("TODO: implement");
    }

    private void evaluateTupleWithHandler(String queryId, ImmutableMap<String, String> bindings, ImmutableList<String> acceptMediaTypes,
                                          ImmutableMultimap<String, String> httpHeaders, BiConsumer<String, String> httpHeaderSetter,
                                          Consumer<Integer> httpStatusSetter, OutputStream outputStream) {
        throw new RuntimeException("TODO: support SELECT queries");
    }
}
