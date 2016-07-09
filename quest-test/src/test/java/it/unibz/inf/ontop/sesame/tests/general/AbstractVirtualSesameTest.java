package it.unibz.inf.ontop.sesame.tests.general;

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.sesame.SesameVirtualRepo;
import junit.framework.TestCase;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.util.Properties;

public abstract class AbstractVirtualSesameTest extends TestCase {

    private final String owlfile;
    private final String r2rmlfile;
    private final Properties properties;

    RepositoryConnection con;

    public AbstractVirtualSesameTest(String owlfile, String r2rmlfile, Properties p) {
        this.owlfile = owlfile;
        this.r2rmlfile = r2rmlfile;
        this.properties = p;
    }

    public void setUp() {
        Repository repo;
        try {
            QuestConfiguration configuration = QuestConfiguration.defaultBuilder()
                    .ontologyFile(owlfile)
                    .r2rmlMappingFile(r2rmlfile)
                    .enableExistentialReasoning(true)
                    .properties(properties)
                    .build();

            repo = new SesameVirtualRepo("virtualExample2", configuration);
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
