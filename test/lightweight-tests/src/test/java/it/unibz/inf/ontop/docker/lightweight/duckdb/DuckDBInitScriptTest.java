package it.unibz.inf.ontop.docker.lightweight.duckdb;

import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.docker.lightweight.AbstractDockerRDF4JTest;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepositoryConnection;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

class DuckDBInitScriptTest  {

    private static OntopRepositoryConnection REPO_CONNECTION;
    private static OntopConnection SQL_CONNECTION;

    private static final String INIT_SCRIPT = "src/test/resources/initFile/duckdb_init.db";
    private static final String OBDA_FILE = "/initFile/duckdb_mapping.obda";
    private static final String PROPERTY_FILE = "/initFile/duckdb.properties";

    @BeforeAll
    public static void before() throws IOException, SQLException, OBDASpecificationException, OntopConnectionException {
        initOBDA(OBDA_FILE, PROPERTY_FILE);
    }

    @AfterAll
    public static void after() throws SQLException, OntopConnectionException {
        release();
    }

    private static void initOBDA(String obdaRelativePath, String propertyFile) throws OBDASpecificationException, OntopConnectionException {

        OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(AbstractDockerRDF4JTest.class.getResource(obdaRelativePath).getPath())
                .propertyFile(AbstractDockerRDF4JTest.class.getResource(propertyFile).getPath())
                .enableTestMode();
        OntopSQLOWLAPIConfiguration config = builder.build();

        OntopVirtualRepository repo = OntopRepository.defaultRepository(config);
        repo.init();
        REPO_CONNECTION = repo.getConnection();
    }

    protected int runQueryAndCount(String queryString) {
        TupleQuery query = REPO_CONNECTION.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

        TupleQueryResult result = query.evaluate();
        int count = 0;
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            count++;
        }
        result.close();
        return count;
    }


    private static void release() throws OntopConnectionException {
        REPO_CONNECTION.close();
    }

    @Test
    void attachDuckDB() throws SQLException {
        Connection sqlConnection = DriverManager.getConnection("jdbc:duckdb:");

        var st = sqlConnection.createStatement();
        st.execute("ATTACH DATABASE '" + INIT_SCRIPT + "' AS duckdb;");

        ResultSet rs = st.executeQuery("SELECT * FROM duckdb.person;");
        while (rs.next()) {
            System.out.println("ID: " + rs.getInt("id"));
            System.out.println("Name: " + rs.getString("name"));
        }
    }

    @Test
    void testEndpoint() {
        String sparqlQuery = "SELECT ?s ?p ?o WHERE { ?s ?p ?o }";
        int count = runQueryAndCount(sparqlQuery);
        assert count == 6;
    }
}
