package it.unibz.inf.ontop.docker;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AbstractRDF4JVirtualModeTest {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static Repository createR2RMLReasoner(String owlFile, String r2rmlFile, String propertiesFile) {
        OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(AbstractRDF4JVirtualModeTest.class.getResource(owlFile).toString())
                .r2rmlMappingFile(AbstractRDF4JVirtualModeTest.class.getResource(r2rmlFile).toString())
                .propertyFile(AbstractRDF4JVirtualModeTest.class.getResource(propertiesFile).toString())
                .enableExistentialReasoning(true)
                .enableTestMode()
                .build();

        Repository repo = OntopRepository.defaultRepository(configuration);
        // Repository must be always initialized first
        repo.init();
        return repo;
    }

    protected static Repository createReasoner(String owlFile, String obdaFile, String propertiesFile) {
        OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(AbstractRDF4JVirtualModeTest.class.getResource(owlFile).toString())
                .nativeOntopMappingFile(AbstractRDF4JVirtualModeTest.class.getResource(obdaFile).toString())
                .propertyFile(AbstractRDF4JVirtualModeTest.class.getResource(propertiesFile).toString())
                .enableExistentialReasoning(true)
                .enableTestMode()
                .build();

        Repository repo = OntopRepository.defaultRepository(configuration);
        // Repository must be always initialized first
        repo.init();
        return repo;
    }

    protected int extecuteQueryAndGetCount(RepositoryConnection con, String query) {
        int resultCount = 0;
        TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
        try (TupleQueryResult result = tupleQuery.evaluate()) {
            while (result.hasNext()) {
                result.next();
                resultCount++;
            }
        }
        return resultCount;
    }

}
