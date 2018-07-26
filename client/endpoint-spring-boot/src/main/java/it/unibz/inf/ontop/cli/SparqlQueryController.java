package it.unibz.inf.ontop.cli;

import java.io.ByteArrayOutputStream;

import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.springframework.web.bind.annotation.*;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;

@RestController
public class SparqlQueryController {

    private final Repository repository;

    public SparqlQueryController() {

        String mappingFile = "/Users/xiao/Documents/ontop-IJCAI-tutorial/obda/university-complete.obda";
        String ontologyFile = "/Users/xiao/Documents/ontop-IJCAI-tutorial/obda/university-complete.ttl";
        String propertyFile = "/Users/xiao/Documents/ontop-IJCAI-tutorial/obda/university-complete.properties";

        this.repository = setupVirtualRepository(mappingFile, ontologyFile, propertyFile);
    }


    @RequestMapping(value = "/sparql", method = {RequestMethod.POST, RequestMethod.GET}, produces = {"application/json"})
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

    private static Repository setupVirtualRepository(String mappings, String ontology, String properties) throws RepositoryException {
        System.out.println(mappings);
        System.out.println(ontology);
        System.out.println(properties);

        OntopRepository repository = OntopRepository
                .defaultRepository(
                        OntopSQLOWLAPIConfiguration.defaultBuilder()
                                .nativeOntopMappingFile(mappings)
                                .ontologyFile(ontology)
                                .propertyFile(properties)
                                .build());

        repository.initialize();

        return repository;

    }
}