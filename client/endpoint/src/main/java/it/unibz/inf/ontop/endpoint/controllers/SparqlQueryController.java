package it.unibz.inf.ontop.endpoint.controllers;

import it.unibz.inf.ontop.endpoint.processor.SparqlQueryExecutor;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@RestController
public class SparqlQueryController {

    private static final Logger log = LoggerFactory.getLogger(SparqlQueryController.class);
    private final SparqlQueryExecutor executor;


    @Autowired
    public SparqlQueryController(OntopVirtualRepository repository) {
        this.executor = new SparqlQueryExecutor(repository);
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
        executor.executeQuery(request, accept, query, defaultGraphUri, namedGraphUri, response);
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
        executor.executeQuery(request, accept, query, defaultGraphUri, namedGraphUri, response);
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
        executor.executeQuery(request, accept, query, defaultGraphUri, namedGraphUri, response);
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
