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

public class DuckDBCatalogTest {
    private final static String OBDA_FILE = "/duckdb-catalog/mapping.obda";
    private final static String PROPERTIES_FILE = "/duckdb-catalog/catalog.properties";
    private static OntopRepositoryConnection CONNECTION;

    @BeforeClass
    public static void before() throws IOException, SQLException {
        CONNECTION = initOBDA(OBDA_FILE, PROPERTIES_FILE);
    }

    @AfterClass
    public static void after() {
        CONNECTION.close();
    }

    @Test
    public void testQuery() {
        String sparqlQuery = "PREFIX ex: <http://example.org/>" +
                "PREFIX cat: <http://example.catalog.org/> " +
                "SELECT DISTINCT ?title WHERE { " +
                "   ?s a ex:Book ;" +
                "       ex:title ?title ." +
                "   ?s1 a cat:Book ;" +
                "       cat:title ?title . }" ;
        int count = runQueryAndCount(sparqlQuery, CONNECTION);
        Assert.assertEquals(4, count);
    }

    private static OntopRepositoryConnection initOBDA(String obdaRelativePath, String propertyFile) {
        OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(DuckDBCatalogTest.class.getResource(obdaRelativePath).getPath())
                .propertyFile(DuckDBCatalogTest.class.getResource(propertyFile).getPath())
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
