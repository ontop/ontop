package it.unibz.inf.ontop.rdf4j.predefined.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.rdf4j.predefined.OntopRDF4JPredefinedQueryEngine;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.rio.RDFHandlerException;

import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class OntopRDF4JPredefinedQueryEngineImpl implements OntopRDF4JPredefinedQueryEngine {


    @Override
    public void evaluate(String queryId, String acceptHeader, ImmutableMap<String, String> bindings,
                         ImmutableMultimap<String, String> httpHeaders,
                         Consumer<Integer> httpStatusSetter,
                         BiConsumer<String, String> httpHeaderSetter,
                         OutputStream outputStream) throws QueryEvaluationException, RDFHandlerException {
        throw new RuntimeException("TODO: implement")
    }

    @Override
    public GraphQueryResult evaluateGraph(String queryId, ImmutableMap<String, String> bindings) throws QueryEvaluationException {
        throw new RuntimeException("TODO: implement");
    }
}
