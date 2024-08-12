package it.unibz.inf.ontop.endpoint.processor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepositoryConnection;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLBooleanJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLBooleanXMLWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.query.resultio.text.BooleanTextWriter;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.eclipse.rdf4j.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.jsonld.JSONLDWriter;
import org.eclipse.rdf4j.rio.rdfjson.RDFJSONWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;
import org.eclipse.rdf4j.rio.nquads.NQuadsWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

public class SparqlQueryExecutor {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final int BAD_REQUEST = 400;
    private static final int NOT_ACCEPTABLE = 406;
    private static final int NOT_IMPLEMENTED = 501;

    private final OntopRepository repository;

    public SparqlQueryExecutor(OntopRepository repository) {
        this.repository = repository;
    }

    public void executeQuery(HttpServletRequest request, String accept, String query,
                             String[] defaultGraphUri, String[] namedGraphUri, HttpServletResponse response) {

        ImmutableMultimap<String, String> httpHeaders = extractHttpHeaders(request);

        try (OntopRepositoryConnection connection = repository.getConnection()) {
            Query q = connection.prepareQuery(QueryLanguage.SPARQL, query, httpHeaders);
            OutputStream bao = response.getOutputStream();

            if (q instanceof TupleQuery) {
                TupleQuery selectQuery = (TupleQuery) q;
                response.setCharacterEncoding("UTF-8");

                if ("*/*".equals(accept) || accept.contains("json")) {
                    response.setHeader(CONTENT_TYPE, "application/sparql-results+json;charset=UTF-8");
                    evaluateSelectQuery(selectQuery, new SPARQLResultsJSONWriter(bao), response);
                } else if (accept.contains("xml")) {
                    response.setHeader(CONTENT_TYPE, "application/sparql-results+xml;charset=UTF-8");
                    evaluateSelectQuery(selectQuery, new SPARQLResultsXMLWriter(bao), response);
                } else if (accept.contains("csv")) {
                    response.setHeader(CONTENT_TYPE, "text/csv;charset=UTF-8");
                    evaluateSelectQuery(selectQuery, new SPARQLResultsCSVWriter(bao), response);
                } else if (accept.contains("tsv") || accept.contains("text/tab-separated-values")) {
                    response.setHeader(CONTENT_TYPE, "text/tab-separated-values;charset=UTF-8");
                    evaluateSelectQuery(selectQuery, new SPARQLResultsTSVWriter(bao), response);
                } else {
                    response.setStatus(NOT_ACCEPTABLE);
                }

            } else if (q instanceof BooleanQuery) {
                BooleanQuery askQuery = (BooleanQuery) q;
                boolean b = askQuery.evaluate();

                if ("*/*".equals(accept) || accept.contains("json")) {
                    response.setHeader(CONTENT_TYPE, "application/sparql-results+json");
                    addCacheHeaders(response);
                    BooleanQueryResultWriter writer = new SPARQLBooleanJSONWriter(bao);
                    writer.handleBoolean(b);
                } else if (accept.contains("xml")) {
                    response.setHeader(CONTENT_TYPE, "application/sparql-results+xml");
                    addCacheHeaders(response);
                    BooleanQueryResultWriter writer = new SPARQLBooleanXMLWriter(bao);
                    writer.handleBoolean(b);
                } else if (accept.contains("text")) {
                    response.setHeader(CONTENT_TYPE, "text/boolean");
                    addCacheHeaders(response);
                    BooleanQueryResultWriter writer = new BooleanTextWriter(bao);
                    writer.handleBoolean(b);
                } else {
                    response.setStatus(NOT_ACCEPTABLE);
                }
            } else if (q instanceof GraphQuery) {
                GraphQuery graphQuery = (GraphQuery) q;
                response.setCharacterEncoding("UTF-8");

                if ("*/*".equals(accept) || accept.contains("turtle")) {
                    response.setHeader(CONTENT_TYPE, "text/turtle;charset=UTF-8");
                    evaluateGraphQuery(graphQuery, new TurtleWriter(bao), response);
                } else if (accept.contains("rdf+json")) {
                    response.setHeader(CONTENT_TYPE, "application/rdf+json;charset=UTF-8");
                    evaluateGraphQuery(graphQuery, new RDFJSONWriter(bao, RDFFormat.RDFJSON), response);
                } else if (accept.contains("json")) {
                    // specification of rdf/json, recommend the use of json-ld (we use it as default)
                    response.setHeader(CONTENT_TYPE, "application/ld+json;charset=UTF-8");
                    evaluateGraphQuery(graphQuery, new JSONLDWriter(bao), response);
                }
                else if (accept.contains("xml")) {
                    response.setHeader(CONTENT_TYPE, "application/rdf+xml;charset=UTF-8");
                    evaluateGraphQuery(graphQuery, new RDFXMLWriter(bao), response);
                }
                else if (accept.contains("n-triples")) {
                    response.setHeader(CONTENT_TYPE, "application/n-triples;charset=UTF-8");
                    evaluateGraphQuery(graphQuery, new NTriplesWriter(bao), response);
                }
                else if (accept.contains("n-quads")) {
                    response.setHeader(CONTENT_TYPE, "application/n-quads;charset=UTF-8");
                    evaluateGraphQuery(graphQuery, new NQuadsWriter(bao), response);
                } else {
                    response.setStatus(NOT_ACCEPTABLE);
                }
            } else if (q instanceof Update) {
                response.setStatus(NOT_IMPLEMENTED);
            } else {
                response.setStatus(BAD_REQUEST);
            }
            bao.flush();
        }
        catch (IOException ex) {
            throw new Error(ex);
        }
    }

    public static ImmutableMultimap<String, String> extractHttpHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .flatMap(k -> Collections.list(request.getHeaders(k)).stream()
                        .map(v -> Maps.immutableEntry(k, v)))
                .collect(ImmutableCollectors.toMultimap());
    }

    private void evaluateSelectQuery(TupleQuery selectQuery, TupleQueryResultWriter writer, HttpServletResponse response) {
        addCacheHeaders(response);
        selectQuery.evaluate(writer);

    }
    private void evaluateGraphQuery(GraphQuery graphQuery, RDFWriter writer, HttpServletResponse response) {
        addCacheHeaders(response);
        graphQuery.evaluate(writer);
    }

    /**
     * TODO: try to find a way to detect if the query is cacheable or not
     * (e.g. not including non-deterministic functions like NOW())
     */
    private void addCacheHeaders(HttpServletResponse response) {
        repository.getHttpCacheHeaders().getMap()
                .forEach(response::setHeader);
    }
}
