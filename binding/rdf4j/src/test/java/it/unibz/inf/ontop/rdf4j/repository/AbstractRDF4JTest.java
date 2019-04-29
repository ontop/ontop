package it.unibz.inf.ontop.rdf4j.repository;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class AbstractRDF4JTest {

    private static final String URL_PREFIX = "jdbc:h2:mem:";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRDF4JTest.class);
    private static Connection SQL_CONNECTION;
    private static RepositoryConnection REPO_CONNECTION;

    protected static void init(String dbScriptRelativePath, String obdaRelativePath) throws SQLException, IOException {

        String jdbcUrl = URL_PREFIX + UUID.randomUUID().toString();

        SQL_CONNECTION = DriverManager.getConnection(jdbcUrl, USER, PASSWORD);

        Statement st = SQL_CONNECTION.createStatement();

        FileReader reader = new FileReader(AbstractRDF4JTest.class.getResource(dbScriptRelativePath).getPath());
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        SQL_CONNECTION.commit();

        OntopSQLOWLAPIConfiguration config = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(AbstractRDF4JTest.class.getResource(obdaRelativePath).getPath())
                .jdbcUrl(jdbcUrl)
                .jdbcUser(USER)
                .jdbcPassword(PASSWORD)
                .enableTestMode()
                .build();

        OntopRepository repo = OntopRepository.defaultRepository(config);
        repo.initialize();
        /*
         * Prepare the data connection for querying.
         */
        REPO_CONNECTION = repo.getConnection();
    }

    protected static void release() throws SQLException {
        REPO_CONNECTION.close();
        SQL_CONNECTION.close();
    }

    protected int runQueryAndCount(String queryString) {
        TupleQuery query = REPO_CONNECTION.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

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

    protected TupleQueryResult evaluate(String queryString) {
        TupleQuery query = REPO_CONNECTION.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
        return query.evaluate();
    }
}
