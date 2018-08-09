package it.unibz.inf.ontop.endpoint;

import java.io.ByteArrayOutputStream;

import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;

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
            method = {RequestMethod.POST, RequestMethod.GET},
            produces = {"application/json"})
    @ResponseBody
    public String query(
            @RequestParam(value = "query") String query) {
        RepositoryConnection connection = repository.getConnection();
        Query q = connection.prepareQuery(QueryLanguage.SPARQL, query);
        TupleQuery selectQuery = (TupleQuery) q;

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        selectQuery.evaluate(new SPARQLResultsJSONWriter(bao));

        return bao.toString();
    }
}