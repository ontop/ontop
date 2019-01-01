package it.unibz.inf.ontop.endpoint;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
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
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@RestController
public class SparqlQueryController {

    private final Repository repository;

    @Autowired
    public SparqlQueryController(EndpointConfig config) {
        this.repository = setupVirtualRepository(config.getMappingFile(), config.getOntologyFile(), config.getPropertiesFile());
    }

    private static Repository setupVirtualRepository(String mappings, String ontology, String properties) throws RepositoryException {
        OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(mappings)
                .ontologyFile(ontology)
                .propertyFile(properties)
                .build();
        OntopRepository repository = OntopRepository.defaultRepository(configuration);

        repository.initialize();

        return repository;
    }

    @RequestMapping(value = "/")
    @ResponseBody
    public Map<String, String> home(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("ontop", "v" + VersionInfo.getVersionInfo().getVersion());
        map.put("HTTP endpoint", request.getRequestURL().toString() + "sparql");
        return map;
    }

    @RequestMapping(value = "/sparql",
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public HttpEntity query_get(
            @RequestHeader(ACCEPT) String accept,
            @RequestParam(value = "query") String query,
            @RequestParam(value = "default-graph-uri", required = false) String[] defaultGraphUri,
            @RequestParam(value = "named-graph-uri", required = false) String[] namedGraphUri) {
        return execQuery(accept, query, defaultGraphUri, namedGraphUri);
    }


    @RequestMapping(value = "/sparql",
            method = RequestMethod.POST,
            consumes = APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public HttpEntity query_post_URL_encoded(
            @RequestHeader(ACCEPT) String accept,
            @RequestParam(value = "query") String query,
            @RequestParam(value = "default-graph-uri", required = false) String[] defaultGraphUri,
            @RequestParam(value = "named-graph-uri", required = false) String[] namedGraphUri) {
        return execQuery(accept, query, defaultGraphUri, namedGraphUri);
    }

    @RequestMapping(value = "/sparql",
            method = RequestMethod.POST,
            consumes = "application/sparql-query")
    @ResponseBody
    public HttpEntity query_post_directly(
            @RequestHeader(ACCEPT) String accept,
            @RequestBody String query,
            @RequestParam(value = "default-graph-uri", required = false) String[] defaultGraphUri,
            @RequestParam(value = "named-graph-uri", required = false) String[] namedGraphUri) {
        return execQuery(accept, query, defaultGraphUri, namedGraphUri);
    }

    private ResponseEntity execQuery(String accept,
                                     String query, String[] defaultGraphUri, String[] namedGraphUri) {

        HttpHeaders headers = new HttpHeaders();

        HttpStatus status = HttpStatus.OK;

        RepositoryConnection connection = repository.getConnection();
        Query q = connection.prepareQuery(QueryLanguage.SPARQL, query);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        final String result;

        if (q instanceof TupleQuery) {
            TupleQuery selectQuery = (TupleQuery) q;

            if ("*/*".equals(accept) || accept.contains("json")) {
                headers.set(HttpHeaders.CONTENT_TYPE, "application/sparql-results+json");
                selectQuery.evaluate(new SPARQLResultsJSONWriter(bao));
                result = bao.toString();
            } else if (accept.contains("xml")) {
                headers.set(HttpHeaders.CONTENT_TYPE, "application/sparql-results+xml");
                selectQuery.evaluate(new SPARQLResultsXMLWriter(bao));
                result = bao.toString();
            } else if (accept.contains("csv")) {
                headers.set(HttpHeaders.CONTENT_TYPE, "text/sparql-results+csv");
                selectQuery.evaluate(new SPARQLResultsCSVWriter(bao));
                result = bao.toString();
            } else if (accept.contains("tsv")) {
                headers.set(HttpHeaders.CONTENT_TYPE, "text/sparql-results+tsv");
                selectQuery.evaluate(new SPARQLResultsTSVWriter(bao));
                result = bao.toString();
            } else {
                result = "";
                status = HttpStatus.BAD_REQUEST;
                //throw new IllegalArgumentException("unsupported ACCEPT : " + accept);
            }

        } else if (q instanceof BooleanQuery) {
            BooleanQuery askQuery = (BooleanQuery) q;
            boolean b = askQuery.evaluate();

            if ("*/*".equals(accept) || accept.contains("json")) {
                headers.set(HttpHeaders.CONTENT_TYPE, "application/sparql-results+json");
                BooleanQueryResultWriter writer = new SPARQLBooleanJSONWriter(bao);
                writer.handleBoolean(b);
                result = bao.toString();
            } else if (accept.contains("xml")) {
                headers.set(HttpHeaders.CONTENT_TYPE, "application/sparql-results+xml");
                BooleanQueryResultWriter writer = new SPARQLBooleanXMLWriter(bao);
                writer.handleBoolean(b);
                result = bao.toString();
            } else if (accept.contains("text")) {
                headers.set(HttpHeaders.CONTENT_TYPE, "text/boolean");
                BooleanQueryResultWriter writer = new BooleanTextWriter(bao);
                writer.handleBoolean(b);
                result = bao.toString();
            } else {
                result = "";
                status = HttpStatus.BAD_REQUEST;
                //throw new IllegalArgumentException("unsupported ACCEPT : " + accept);
            }
        } else if (q instanceof GraphQuery) {
            GraphQuery graphQuery = (GraphQuery) q;
            if ("*/*".equals(accept) || accept.contains("turtle")) {
                headers.set(HttpHeaders.CONTENT_TYPE, "text/turtle");
                graphQuery.evaluate(new TurtleWriter(bao));
                result = bao.toString();
            } else if (accept.contains("json")) {
                headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
                graphQuery.evaluate(new org.eclipse.rdf4j.rio.rdfjson.RDFJSONWriter(bao, RDFFormat.JSONLD));
                result = bao.toString();
            } else if (accept.contains("xml")) {
                headers.set(HttpHeaders.CONTENT_TYPE, "application/rdf+xml");
                graphQuery.evaluate(new RDFXMLWriter(bao));
                result = bao.toString();
            } else {
                //throw new IllegalArgumentException("unsupported ACCEPT : " + accept);
                result = "";
                status = HttpStatus.BAD_REQUEST;
            }
        } else if (q instanceof Update) {
            //else if (q instanceof Update)
            result = "";
            status = HttpStatus.NOT_IMPLEMENTED;
        } else {
            result = "";
            status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(result, headers, status);
    }


    @ExceptionHandler({MalformedQueryException.class})
    public ResponseEntity handleMalformedQueryException(Exception ex) {
        String message = ex.getMessage();
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(message, headers, status);
    }

    @ExceptionHandler({RepositoryException.class, Exception.class})
    public ResponseEntity handleRepositoryException(Exception ex) {
        String message = ex.getMessage();
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(message, headers, status);
    }

}
