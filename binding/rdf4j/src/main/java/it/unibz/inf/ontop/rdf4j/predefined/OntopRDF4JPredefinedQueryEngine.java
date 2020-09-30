package it.unibz.inf.ontop.rdf4j.predefined;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.rio.RDFHandlerException;

import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * TODO: find a better name
 */
public interface OntopRDF4JPredefinedQueryEngine {

    void evaluate(String queryId, String acceptHeader,
                  ImmutableMap<String, String> bindings,
                  ImmutableMultimap<String, String> httpHeaders,
                  Consumer<Integer> httpStatusSetter,
                  BiConsumer<String, String> httpHeaderSetter,
                  OutputStream outputStream) throws QueryEvaluationException, RDFHandlerException;

    GraphQueryResult evaluateGraph(String queryId, ImmutableMap<String, String> bindings) throws QueryEvaluationException;

    // TODO: add methods for select and ask

}
