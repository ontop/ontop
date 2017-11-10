package it.unibz.inf.ontop.docker;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import junit.framework.TestCase;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

public abstract class AbstractVirtualSesameTest extends TestCase {

    private final String owlFileName;
    private final String r2rmlFileName;
    private final String propertyFileName;

    RepositoryConnection con;

    public AbstractVirtualSesameTest(String owlFile, String r2rmlFile, String propertyFile) {
        this.owlFileName =  this.getClass().getResource(owlFile).toString();
        this.r2rmlFileName =  this.getClass().getResource(r2rmlFile).toString();
        this.propertyFileName =  this.getClass().getResource(propertyFile).toString();
    }

    public void setUp() {
        Repository repo;
        try {
            OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                    .ontologyFile(owlFileName)
                    .r2rmlMappingFile(r2rmlFileName)
                    .enableExistentialReasoning(true)
                    .propertyFile(propertyFileName)
                    .enableTestMode()
                    .build();

            repo = OntopRepository.defaultRepository(configuration);
			/*
			 * Repository must be always initialized first
			 */
            repo.initialize();

			/*
			 * Get the repository connection
			 */
            con = repo.getConnection();

        } catch (Exception e) {
            e.printStackTrace();
            assertFalse(false);
        }

    }

    public void tearDown() {
        try {
            if (con != null && con.isOpen()) {
                con.close();
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    protected int count(String query){
        int resultCount = 0;
        try {
            TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,
                    query);
            TupleQueryResult result = tupleQuery.evaluate();

            while (result.hasNext()) {
                result.next();
                resultCount++;
            }

            result.close();

        } catch (Exception e) {
            e.printStackTrace();
            assertFalse(false);
        }
        return resultCount++;
    }
}
