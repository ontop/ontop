package federationOptimization;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;

import static java.util.stream.Collectors.joining;

public class LensesTest {

//    private final static String OWL_FILE = "src/test/resources/lenses-test/vkg.owl";
//    private final static String OBDA_FILE = "src/test/resources/lenses-test/vkg.obda";
//    private final static String PROPERTY_FILE = "src/test/resources/lenses-test/vkg.properties";
//    private final static String LENSES_FILE = "src/test/resources/lenses-test/vkg_lenses.json";
//    private final static String TEST_QUERY="src/test/resources/lenses-test/queries/00.sql";

    //BSBM-federation test
    private final static String OWL_FILE = "src/test/resources/lenses-test/bsbm-ontology.owl";
    private final static String OBDA_FILE = "src/test/resources/lenses-test/bsbm-mappings-hom-het.obda";
    private final static String PROPERTY_FILE = "src/test/resources/lenses-test/teiid.properties";
    private final static String LENSES_FILE = "src/test/resources/lenses-test/bsbm_lenses.json";
    private final static String TEST_QUERY="src/test/resources/lenses-test/queries/00.sql";

    private RepositoryConnection conn;

    @Before
    public void setUpOntop() {

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .propertyFile(PROPERTY_FILE)
                .nativeOntopMappingFile(OBDA_FILE)
                .ontologyFile(OWL_FILE)
                .lensesFile(LENSES_FILE)
                .enableTestMode()
                .build();

        OntopRepository repo = OntopRepository.defaultRepository(config);
        repo.init();
        /*
         * Prepare the data connection for querying.
         */
        conn = repo.getConnection();
    }

    @Test
    public void testSELECT(){

        try {
            String queryString = Files.lines(Paths.get(TEST_QUERY)).collect(joining("\n"));

            // execute query
            TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            TupleQueryResult result = query.evaluate();
            int count = 0;
            while (result.hasNext()) {
                result.next();
                count++;
            }
            result.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
