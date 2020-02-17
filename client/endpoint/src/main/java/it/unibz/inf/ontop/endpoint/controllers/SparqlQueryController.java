package it.unibz.inf.ontop.endpoint.controllers;

import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import it.unibz.inf.ontop.utils.VersionInfo;

import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLBooleanJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLBooleanXMLWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.query.resultio.text.BooleanTextWriter;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.eclipse.rdf4j.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.rdfjson.RDFJSONWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
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
            HttpServletResponse response) {
        execQuery(accept, query, defaultGraphUri, namedGraphUri, response);
    }

    @RequestMapping(value = "/sparql",
            method = RequestMethod.POST,
            consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public void query_post_URL_encoded(
            @RequestHeader(ACCEPT) String accept,
            @RequestParam(value = "query") String query,
            @RequestParam(value = "default-graph-uri", required = false) String[] defaultGraphUri,
            @RequestParam(value = "named-graph-uri", required = false) String[] namedGraphUri,
            HttpServletResponse response) {
        execQuery(accept, query, defaultGraphUri, namedGraphUri, response);
    }

    @RequestMapping(value = "/sparql",
            method = RequestMethod.POST,
            consumes = "application/sparql-query")
    public void query_post_directly(
            @RequestHeader(ACCEPT) String accept,
            @RequestBody String query,
            @RequestParam(value = "default-graph-uri", required = false) String[] defaultGraphUri,
            @RequestParam(value = "named-graph-uri", required = false) String[] namedGraphUri,
            HttpServletResponse response) {
        execQuery(accept, query, defaultGraphUri, namedGraphUri, response);
    }

    private void execQuery(String accept, String query, String[] defaultGraphUri, String[] namedGraphUri,
                           HttpServletResponse response) {
        try (RepositoryConnection connection = repository.getConnection()) {
            Query q = connection.prepareQuery(QueryLanguage.SPARQL, query);
            OutputStream bao = response.getOutputStream();

            if (q instanceof TupleQuery) {
                TupleQuery selectQuery = (TupleQuery) q;
                response.setCharacterEncoding("UTF-8");

                if ("*/*".equals(accept) || accept.contains("json")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/sparql-results+json;charset=UTF-8");
                    selectQuery.evaluate(new SPARQLResultsJSONWriter(bao));
                } else if (accept.contains("xml")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/sparql-results+xml;charset=UTF-8");
                    selectQuery.evaluate(new SPARQLResultsXMLWriter(bao));
                } else if (accept.contains("csv")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "text/sparql-results+csv;charset=UTF-8");
                    selectQuery.evaluate(new SPARQLResultsCSVWriter(bao));
                } else if (accept.contains("tsv")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "text/sparql-results+tsv;charset=UTF-8");
                    selectQuery.evaluate(new SPARQLResultsTSVWriter(bao));
                } else {
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                }

            } else if (q instanceof BooleanQuery) {
                BooleanQuery askQuery = (BooleanQuery) q;
                boolean b = askQuery.evaluate();

                if ("*/*".equals(accept) || accept.contains("json")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/sparql-results+json");
                    BooleanQueryResultWriter writer = new SPARQLBooleanJSONWriter(bao);
                    writer.handleBoolean(b);
                } else if (accept.contains("xml")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/sparql-results+xml");
                    BooleanQueryResultWriter writer = new SPARQLBooleanXMLWriter(bao);
                    writer.handleBoolean(b);
                } else if (accept.contains("text")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "text/boolean");
                    BooleanQueryResultWriter writer = new BooleanTextWriter(bao);
                    writer.handleBoolean(b);
                } else {
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                }
            } else if (q instanceof GraphQuery) {
                GraphQuery graphQuery = (GraphQuery) q;
                response.setCharacterEncoding("UTF-8");

                if ("*/*".equals(accept) || accept.contains("turtle")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "text/turtle;charset=UTF-8");
                    graphQuery.evaluate(new TurtleWriter(bao));
                } else if (accept.contains("json")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
                    graphQuery.evaluate(new RDFJSONWriter(bao, RDFFormat.JSONLD));
                } else if (accept.contains("xml")) {
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/rdf+xml;charset=UTF-8");
                    graphQuery.evaluate(new RDFXMLWriter(bao));
                } else {
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
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
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(message, headers, status);
    }

}
