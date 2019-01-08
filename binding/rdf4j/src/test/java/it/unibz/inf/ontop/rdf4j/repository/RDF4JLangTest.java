package it.unibz.inf.ontop.rdf4j.repository;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;


public class RDF4JLangTest {

    private static final String URL = "jdbc:h2:mem:lang";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static final String CREATE_DB_FILE = "/label_comment.sql";
    private static final String OBDA_FILE = "/label_comment.obda";
    private static final Logger LOGGER = LoggerFactory.getLogger(RDF4JLangTest.class);
    private static Connection SQL_CONN;
    private static RepositoryConnection CONN;

    @BeforeClass
    public static void setUp() throws Exception {

        SQL_CONN = DriverManager.getConnection(URL, USER, PASSWORD);

        Statement st = SQL_CONN.createStatement();

        FileReader reader = new FileReader(RDF4JLangTest.class.getResource(CREATE_DB_FILE).getPath());
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        SQL_CONN.commit();

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(RDF4JLangTest.class.getResource(OBDA_FILE).getPath())
                .jdbcUrl(URL)
                .jdbcUser(USER)
                .jdbcPassword(PASSWORD)
                .enableTestMode()
                .build();

        OntopRepository repo = OntopRepository.defaultRepository(config);
        repo.initialize();
        /*
         * Prepare the data connection for querying.
         */
        CONN = repo.getConnection();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        CONN.close();
        SQL_CONN.close();
    }

    @Test
    public void testSameLanguageLabelComment() {
        String query = "SELECT  *\n" +
                "WHERE {\n" +
                "  ?o rdfs:label ?label ;\n" +
                "     rdfs:comment ?comment .\n" +
                "  FILTER( LANG(?label) = LANG(?comment) )\n" +
                "}";
        int count = runQuery(query);
        assertEquals(2, count);
    }

    protected int runQuery(String queryString) {
        TupleQuery query = CONN.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

        TupleQueryResult result = query.evaluate();
        int count = 0;
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            LOGGER.debug(bindingSet + "\n");
            count++;
        }
        result.close();
        return count;
    }
}
