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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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
    private volatile boolean initialized = false;

    @Autowired
    public SparqlQueryController(@Value("${mapping}") String mappingFile,
                                 @Value("${properties}") String propertiesFile,
                                 @Value("${lazy:false}") boolean lazy,
                                 @Value("${ontology:#{null}}") String owlFile) {
        this.repository = setupVirtualRepository(mappingFile, owlFile, propertiesFile, lazy);
    }

    private Repository setupVirtualRepository(String mappings, String ontology, String properties, boolean lazy) throws RepositoryException {
        OntopSQLOWLAPIConfiguration.Builder<? extends OntopSQLOWLAPIConfiguration.Builder> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .propertyFile(properties);

        if (mappings.endsWith(".obda"))
            builder.nativeOntopMappingFile(mappings);
        else
            builder.r2rmlMappingFile(mappings);

        if ((ontology != null) && (!ontology.isEmpty()))
            builder.ontologyFile(ontology);

        OntopSQLOWLAPIConfiguration configuration = builder.build();
        OntopRepository repository = OntopRepository.defaultRepository(configuration);

        if (!lazy) {
            repository.initialize();
            this.initialized = true;
        }

        return repository;
    }

    @GetMapping(value = "/")
    public ModelAndView home(HttpServletRequest request) {
        Map<String, String> model = new HashMap<>();
        model.put("version", VersionInfo.getVersionInfo().getVersion());
        model.put("endpointUrl", request.getRequestURL().toString() + "sparql");
        model.put("yasguiUrl", request.getRequestURL().toString() + "yasgui");
        return new ModelAndView("index", model);
    }

    @RequestMapping(value = "/sparql",
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public HttpEntity<String> query_get(
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
    public HttpEntity<String> query_post_URL_encoded(
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
    public HttpEntity<String> query_post_directly(
            @RequestHeader(ACCEPT) String accept,
            @RequestBody String query,
            @RequestParam(value = "default-graph-uri", required = false) String[] defaultGraphUri,
            @RequestParam(value = "named-graph-uri", required = false) String[] namedGraphUri) {
        return execQuery(accept, query, defaultGraphUri, namedGraphUri);
    }

    private ResponseEntity<String> execQuery(String accept,
                                             String query, String[] defaultGraphUri, String[] namedGraphUri) {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    repository.initialize();
                    initialized = true;
                }
            }
        }

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
