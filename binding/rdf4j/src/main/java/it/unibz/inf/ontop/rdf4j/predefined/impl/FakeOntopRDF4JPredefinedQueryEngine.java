package it.unibz.inf.ontop.rdf4j.predefined.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.rdf4j.predefined.OntopRDF4JPredefinedQueryEngine;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.rio.RDFHandlerException;

import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FakeOntopRDF4JPredefinedQueryEngine implements OntopRDF4JPredefinedQueryEngine {

    @Override
    public void evaluate(String queryId, ImmutableMap<String, String> bindings, ImmutableList<String> acceptMediaTypes,
                         ImmutableMultimap<String, String> httpHeaders, Consumer<Integer> httpStatusSetter,
                         BiConsumer<String, String> httpHeaderSetter, OutputStream outputStream)
            throws QueryEvaluationException, RDFHandlerException {
        // Not-recognized query id
        httpStatusSetter.accept(404);
    }

    @Override
    public String evaluate(String queryId, ImmutableMap<String, String> bindings, ImmutableList<String> acceptMediaTypes,
                           ImmutableMultimap<String, String> httpHeaders, Consumer<Integer> httpStatusSetter,
                           BiConsumer<String, String> httpHeaderSetter) {
        // Not-recognized query id
        httpStatusSetter.accept(404);
        return "Not found";
    }

    @Override
    public boolean shouldStream(String queryId) {
        return false;
    }

    @Override
    public GraphQueryResult evaluateGraph(String queryId, ImmutableMap<String, String> bindings) throws QueryEvaluationException {
        throw new UnsupportedOperationException("Intended to be used by the HTTP endpoint only");
    }
}
