package it.unibz.inf.ontop.rdf4j.repository;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class InitializationScriptTest  {

    private static final String OBDA_FILE = "/initScript/person.obda";
    private static final String H2_PROPERTIES_FILE = "/initScript/h2.properties";
    private static final String PROPERTIES_FILE = "/initScript/duckdb.properties";
    private static OntopRepositoryConnection DUCKDB_CONNECTION;
    private static OntopRepositoryConnection H2_CONNECTION;

    @BeforeClass
    public static void before() throws IOException, SQLException {
        DUCKDB_CONNECTION = initOBDA(OBDA_FILE, PROPERTIES_FILE);
        H2_CONNECTION = initOBDA(OBDA_FILE, H2_PROPERTIES_FILE);
    }

    @AfterClass
    public static void after() {
        DUCKDB_CONNECTION.close();
        H2_CONNECTION.close();
    }

@Test
    public void testQueryH2() {
        String sparqlQuery = "PREFIX : <http://example.org/>" +
                "SELECT ?s WHERE { ?s :name ?o }";
        int count = runQueryAndCount(sparqlQuery, H2_CONNECTION);
        Assert.assertEquals(3, count);
    }

    @Test
    public void testQueryOnNullableColumnH2() {
        String sparqlQuery = "PREFIX : <http://example.org/>" +
                "SELECT ?s WHERE { ?s :age ?o }";
        int count = runQueryAndCount(sparqlQuery, H2_CONNECTION);
        Assert.assertEquals(2, count);
    }

    @Test
    public void testPrimaryKeyConstraintH2() {
        String sparqlQuery = "PREFIX : <http://example.org/>" +
                "SELECT ?s WHERE { ?s :name ?o }";
        String query = H2_CONNECTION.reformulateIntoNativeQuery(sparqlQuery);
        Assert.assertFalse(query.contains("DISTINCT"));
    }

    @Test
    public void testQueryDuckDB() {
        String sparqlQuery = "PREFIX : <http://example.org/>" +
                "SELECT ?s WHERE { ?s :name ?o }";
        int count = runQueryAndCount(sparqlQuery, DUCKDB_CONNECTION);
        Assert.assertEquals(3, count);
    }

    @Test
    public void testQueryOnNullableColumnDuckDB() {
        String sparqlQuery = "PREFIX : <http://example.org/>" +
                "SELECT ?s WHERE { ?s :age ?o }";
        int count = runQueryAndCount(sparqlQuery, DUCKDB_CONNECTION);
        Assert.assertEquals(2, count);
    }

    @Test
    public void testPrimaryKeyConstraintDuckDB() {
        String sparqlQuery = "PREFIX : <http://example.org/>" +
                "SELECT ?s WHERE { ?s :name ?o }";
        String query = DUCKDB_CONNECTION.reformulateIntoNativeQuery(sparqlQuery);
        Assert.assertFalse(query.contains("DISTINCT"));
    }

    private static OntopRepositoryConnection initOBDA(String obdaRelativePath, String propertyFile) {
        OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(AbstractRDF4JTest.class.getResource(obdaRelativePath).getPath())
                .propertyFile(AbstractRDF4JTest.class.getResource(propertyFile).getPath())
                .enableTestMode();

        OntopSQLOWLAPIConfiguration config = builder.build();

        OntopVirtualRepository repo = OntopRepository.defaultRepository(config);
        repo.init();

        return repo.getConnection();
    }

    private int runQueryAndCount(String queryString, OntopRepositoryConnection connection) {
        TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

        TupleQueryResult result = query.evaluate();
        int count = 0;
        while (result.hasNext()) {
            result.next();
            count++;
        }
        result.close();
        return count;
    }
    
}
