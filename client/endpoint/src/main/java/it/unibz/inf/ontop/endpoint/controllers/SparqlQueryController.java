package it.unibz.inf.ontop.endpoint.controllers;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopRepositoryConnection;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VersionInfo;

import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLBooleanJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLBooleanXMLWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.query.resultio.text.BooleanTextWriter;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.eclipse.rdf4j.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.jsonld.JSONLDWriter;
import org.eclipse.rdf4j.rio.rdfjson.RDFJSONWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@RestController
public class SparqlQueryController {

    private static final Logger log = LoggerFactory.getLogger(SparqlQueryController.class);

    private final OntopVirtualRepository repository;

    @Autowired
    public SparqlQueryController(OntopVirtualRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "/")
    public ModelAndView home() {
        Map<String, String> model = new HashMap<>();
        model.put("version", VersionInfo.getVersionInfo().getVersion());
        return new ModelAndView("index", model);
    }

    @RequestMapping(value = "/sparql",
            method = {RequestMethod.GET}
    )
    public void query_get(
            @RequestHeader(ACCEPT) String accept,
            @RequestParam(value = "query") String query,
            @RequestParam(value = "default-graph-uri", required = false) String[] defaultGraphUri,
            @RequestParam(value = "named-graph-uri", required = false) String[] namedGraphUri,
            HttpServletRequest request, HttpServletResponse response) {
        execQuery(request, accept, query, defaultGraphUri, namedGraphUri, response);
    }

    @RequestMapping(value = "/sparql",
            method = RequestMethod.POST,
            consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public void query_post_URL_encoded(
            @RequestHeader(ACCEPT) String accept,
            @RequestParam(value = "query") String query,
            @RequestParam(value = "default-graph-uri", required = false) String[] defaultGraphUri,
            @RequestParam(value = "named-graph-uri", required = false) String[] namedGraphUri,
            HttpServletRequest request, HttpServletResponse response) {
        execQuery(request, accept, query, defaultGraphUri, namedGraphUri, response);
    }

    @RequestMapping(value = "/sparql",
            method = RequestMethod.POST,
            consumes = "application/sparql-query")
    public void query_post_directly(
            @RequestHeader(ACCEPT) String accept,
            @RequestBody String query,
            @RequestParam(value = "default-graph-uri", required = false) String[] defaultGraphUri,
            @RequestParam(value = "named-graph-uri", required = false) String[] namedGraphUri,
            HttpServletRequest request, HttpServletResponse response) {
        execQuery(request, accept, query, defaultGraphUri, namedGraphUri, response);
    }

    private void execQuery(HttpServletRequest request, String accept, String query, String[] defaultGraphUri, String[] namedGraphUri,
                           HttpServletResponse response) {

        ImmutableMultimap<String, String> httpHeaders = Collections.list(request.getHeaderNames()).stream()
                .flatMap(k -> Collections.list(request.getHeaders(k)).stream()
                        .map(v -> Maps.immutableEntry(k, v)))
                .collect(ImmutableCollectors.toMultimap());

        try (OntopRepositoryConnection connection = repository.getConnection()) {
            Query q = connection.prepareQuery(QueryLanguage.SPARQL, query, httpHeaders);
            OutputStream bao = response.getOutputStream();

            if (q instanceof TupleQuery) {
                TupleQuery selectQuery = (TupleQuery) q;
                response.setCharacterEncoding("UTF-8");

                if ("*/*".equals(accept) || accept.contains("json")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/sparql-results+json;charset=UTF-8");
                    evaluateSelectQuery(selectQuery, new SPARQLResultsJSONWriter(bao), response);
                } else if (accept.contains("xml")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/sparql-results+xml;charset=UTF-8");
                    evaluateSelectQuery(selectQuery, new SPARQLResultsXMLWriter(bao), response);
                } else if (accept.contains("csv")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "text/sparql-results+csv;charset=UTF-8");
                    evaluateSelectQuery(selectQuery, new SPARQLResultsCSVWriter(bao), response);
                } else if (accept.contains("tsv") || accept.contains("text/tab-separated-values")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "text/sparql-results+tsv;charset=UTF-8");
                    evaluateSelectQuery(selectQuery, new SPARQLResultsTSVWriter(bao), response);
                } else {
                    response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
                }

            } else if (q instanceof BooleanQuery) {
                BooleanQuery askQuery = (BooleanQuery) q;
                boolean b = askQuery.evaluate();

                if ("*/*".equals(accept) || accept.contains("json")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/sparql-results+json");
                    addCacheHeaders(response);
                    BooleanQueryResultWriter writer = new SPARQLBooleanJSONWriter(bao);
                    writer.handleBoolean(b);
                } else if (accept.contains("xml")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/sparql-results+xml");
                    addCacheHeaders(response);
                    BooleanQueryResultWriter writer = new SPARQLBooleanXMLWriter(bao);
                    writer.handleBoolean(b);
                } else if (accept.contains("text")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "text/boolean");
                    addCacheHeaders(response);
                    BooleanQueryResultWriter writer = new BooleanTextWriter(bao);
                    writer.handleBoolean(b);
                } else {
                    response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
                }
            } else if (q instanceof GraphQuery) {
                GraphQuery graphQuery = (GraphQuery) q;
                response.setCharacterEncoding("UTF-8");

                if ("*/*".equals(accept) || accept.contains("turtle")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "text/turtle;charset=UTF-8");
                    evaluateGraphQuery(graphQuery, new TurtleWriter(bao), response);
                } else if (accept.contains("rdf+json")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/rdf+json;charset=UTF-8");
                    evaluateGraphQuery(graphQuery, new RDFJSONWriter(bao, RDFFormat.RDFJSON), response);
                } else if (accept.contains("json")) {
                    // specification of rdf/json, recommend the use of json-ld (we use it as default)
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/ld+json;charset=UTF-8");
                    evaluateGraphQuery(graphQuery, new JSONLDWriter(bao), response);
                }
                else if (accept.contains("xml")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/rdf+xml;charset=UTF-8");
                    evaluateGraphQuery(graphQuery, new RDFXMLWriter(bao), response);
                } else {
                    response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
                }
            } else if (q instanceof Update) {
                response.setStatus(HttpStatus.NOT_IMPLEMENTED.value());
            } else {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
            }
            bao.flush();
        }
        catch (IOException ex) {
            throw new Error(ex);
        }
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

    @ExceptionHandler({MalformedQueryException.class})
    public ResponseEntity<String> handleMalformedQueryException(Exception ex) {
        ex.printStackTrace();
        String message = ex.getMessage();
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(message, headers, status);
    }

    @ExceptionHandler({RepositoryException.class, Exception.class})
    public ResponseEntity<String> handleRepositoryException(Exception ex) {
        ex.printStackTrace();
        String message = ex.getMessage();
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        // Invalidates the previous Cache-Control header
        headers.set(CACHE_CONTROL, "no-store");
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(message, headers, status);
    }

}
