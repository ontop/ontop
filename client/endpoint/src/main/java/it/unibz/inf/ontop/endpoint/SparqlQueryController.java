package it.unibz.inf.ontop.endpoint;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import org.eclipse.rdf4j.http.protocol.Protocol;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;

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


    @RequestMapping(value = "/sparql",
            method = {RequestMethod.GET},
            produces = {"application/json"})
    @ResponseBody
    public String query_get(
            @RequestParam(value = "query") String query,
            @RequestParam(value = "default-graph-uri") String[] defaultGraphUri,
            @RequestParam(value = "named-graph-uri") String[] namedGraphUri
    ) {
        return execQuery(query, defaultGraphUri, namedGraphUri);
    }


    @RequestMapping(value = "/sparql",
            method = RequestMethod.POST,
            consumes = "application/x-www-form-urlencoded",
            produces = {"application/json"})
    @ResponseBody
    public String query_post_URL_encoded(
            @RequestParam(value = "query") String query,
            @RequestParam(value = "default-graph-uri", required = false) String[] defaultGraphUri,
            @RequestParam(value = "named-graph-uri", required = false) String[] namedGraphUri) {
        return execQuery(query, defaultGraphUri, namedGraphUri);
    }

    @RequestMapping(value = "/sparql",
            method = RequestMethod.POST,
            consumes = "application/sparql-query",
            produces = {"application/json"})
    @ResponseBody
    public String query_post_directly(
            @RequestBody String query,
            @RequestParam(value = "default-graph-uri", required = false) String[] defaultGraphUri,
            @RequestParam(value = "named-graph-uri", required = false) String[] namedGraphUri) {
        return execQuery(query, defaultGraphUri, namedGraphUri);
    }

    private String execQuery(String query, String[] defaultGraphUri, String[] namedGraphUri) {
        RepositoryConnection connection = repository.getConnection();
        Query q = connection.prepareQuery(QueryLanguage.SPARQL, query);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        String result;

        if (q instanceof TupleQuery) {
            TupleQuery selectQuery = (TupleQuery) q;
            selectQuery.evaluate(new SPARQLResultsJSONWriter(bao));
            result = bao.toString();
        } else if (q instanceof BooleanQuery) {
            BooleanQuery askQuery = (BooleanQuery) q;
            boolean b = askQuery.evaluate();
            result = String.format("{\"boolean\" : %s \n}", b);
        } else if (q instanceof GraphQuery) {
            GraphQuery graphQuery = ((GraphQuery) q);
            graphQuery.evaluate(new org.eclipse.rdf4j.rio.rdfjson.RDFJSONWriter(bao, RDFFormat.JSONLD));
            result = bao.toString();
        } else if (q instanceof GraphQuery) {
            //else if (q instanceof Update)
            throw new UnsupportedOperationException("Unsupported query");
        } else {
            throw new UnsupportedOperationException();

        }
        return result;
    }
}