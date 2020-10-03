package it.unibz.inf.ontop.rdf4j.predefined;

import com.google.common.collect.ImmutableList;
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

    /**
     * acceptMediaTypes are expected to be sorted by decreasing importance and having no quality parameter
     *
     * May throw RDF4J runtime exceptions when executing the query.
     * Problems occurring before query execution are handled by standard HTTP mechanisms (status code + error message).
     *
     * NB: this method prototype could easily be made RDF4J-independent, if the need appears one day.
     */
    void evaluate(String queryId,
                  ImmutableMap<String, String> bindings,
                  ImmutableList<String> acceptMediaTypes,
                  ImmutableMultimap<String, String> httpHeaders,
                  Consumer<Integer> httpStatusSetter,
                  BiConsumer<String, String> httpHeaderSetter,
                  OutputStream outputStream) throws QueryEvaluationException, RDFHandlerException;

    GraphQueryResult evaluateGraph(String queryId, ImmutableMap<String, String> bindings) throws QueryEvaluationException;

    // TODO: add methods for select and ask

}
