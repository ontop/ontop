package it.unibz.inf.ontop;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopVirtualRepository;
import junit.framework.TestCase;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

public abstract class AbstractVirtualSesameTest extends TestCase {

    private final String owlfile;
    private final String r2rmlfile;
    private final String propertyFile;

    RepositoryConnection con;

    public AbstractVirtualSesameTest(String owlfile, String r2rmlfile, String propertyFile) {
        this.owlfile = owlfile;
        this.r2rmlfile = r2rmlfile;
        this.propertyFile = propertyFile;
    }

    public void setUp() {
        Repository repo;
        try {
            OntopSQLOWLAPIConfiguration configuration = OntopSQLOWLAPIConfiguration.defaultBuilder()
                    .ontologyFile(owlfile)
                    .r2rmlMappingFile(r2rmlfile)
                    .enableExistentialReasoning(true)
                    .propertyFile(propertyFile)
                    .enableTestMode()
                    .build();

            repo = new OntopVirtualRepository(configuration);
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
