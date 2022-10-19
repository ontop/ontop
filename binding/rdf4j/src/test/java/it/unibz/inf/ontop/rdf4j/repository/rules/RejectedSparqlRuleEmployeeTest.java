package it.unibz.inf.ontop.rdf4j.repository.rules;

import it.unibz.inf.ontop.exception.SparqlRuleException;
import it.unibz.inf.ontop.rdf4j.repository.H2RDF4JTestTools;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RejectedSparqlRuleEmployeeTest {

    private static final String OBDA_FILE = "/employee/employee.obda";
    private static final String SQL_SCRIPT = "/employee/employee.sql";

    private static final String JDBC_URL;
    private static Connection SQL_CONNECTION;

    static {
        JDBC_URL = H2RDF4JTestTools.generateJdbcUrl();
    }


    @BeforeClass
    public static void before() throws IOException, SQLException {
        SQL_CONNECTION = H2RDF4JTestTools.createH2Instance(JDBC_URL, SQL_SCRIPT);
    }

    @AfterClass
    public static void after() throws SQLException {
        SQL_CONNECTION.close();
    }

    @Test
    public void testCyclingRules1() {
        try(OntopRepositoryConnection ignored = H2RDF4JTestTools.initOBDA(JDBC_URL, OBDA_FILE, null, null, null, null,
                "/employee/rejected/cyclic-rules-1.toml")) {
            fail("A repository exception was expected");
        } catch (RepositoryException e) {
            assertTrue(e.getCause() instanceof SparqlRuleException);
        }
    }

    @Test
    public void testMetaPropertyInBody() {
        try(OntopRepositoryConnection ignored = H2RDF4JTestTools.initOBDA(JDBC_URL, OBDA_FILE, null, null, null, null,
                "/employee/rejected/meta-property-body-rule.toml")) {
            fail("A repository exception was expected");
        } catch (RepositoryException e) {
            assertTrue(e.getCause() instanceof SparqlRuleException);
        }
    }

    @Test
    public void testMetaPropertyInHead() {
        try(OntopRepositoryConnection ignored = H2RDF4JTestTools.initOBDA(JDBC_URL, OBDA_FILE, null, null, null, null,
                "/employee/rejected/meta-property-head-rule.toml")) {
            fail("A repository exception was expected");
        } catch (RepositoryException e) {
            assertTrue(e.getCause() instanceof SparqlRuleException);
        }
    }

    @Test
    public void testMetaClassInBody() {
        try(OntopRepositoryConnection ignored = H2RDF4JTestTools.initOBDA(JDBC_URL, OBDA_FILE, null, null, null, null,
                "/employee/rejected/meta-class-body-rule.toml")) {
            fail("A repository exception was expected");
        } catch (RepositoryException e) {
            assertTrue(e.getCause() instanceof SparqlRuleException);
        }
    }

    @Test
    public void testMetaClassInHead() {
        try(OntopRepositoryConnection ignored = H2RDF4JTestTools.initOBDA(JDBC_URL, OBDA_FILE, null, null, null, null,
                "/employee/rejected/meta-class-head-rule.toml")) {
            fail("A repository exception was expected");
        } catch (RepositoryException e) {
            assertTrue(e.getCause() instanceof SparqlRuleException);
        }
    }

    @Test
    public void testInvalidInsertRule() {
        try(OntopRepositoryConnection ignored = H2RDF4JTestTools.initOBDA(JDBC_URL, OBDA_FILE, null, null, null, null,
                "/employee/rejected/invalid-insert.toml")) {
            fail("A repository exception was expected");
        } catch (RepositoryException e) {
            assertTrue(e.getCause() instanceof SparqlRuleException);
        }
    }

    @Test
    public void testUnsupportedInsertDeleteRule() {
        try(OntopRepositoryConnection ignored = H2RDF4JTestTools.initOBDA(JDBC_URL, OBDA_FILE, null, null, null, null,
                "/employee/rejected/invalid-insert-delete.toml")) {
            fail("A repository exception was expected");
        } catch (RepositoryException e) {
            assertTrue(e.getCause() instanceof SparqlRuleException);
        }
    }

    @Test
    public void testConstructRule() {
        try(OntopRepositoryConnection ignored = H2RDF4JTestTools.initOBDA(JDBC_URL, OBDA_FILE, null, null, null, null,
                "/employee/rejected/construct-rule.toml")) {
            fail("A repository exception was expected");
        } catch (RepositoryException e) {
            assertTrue(e.getCause() instanceof SparqlRuleException);
        }
    }

    @Test
    public void testNoRuleEntries() {
        try(OntopRepositoryConnection ignored = H2RDF4JTestTools.initOBDA(JDBC_URL, OBDA_FILE, null, null, null, null,
                "/employee/rejected/no-rules.toml")) {
            fail("A repository exception was expected");
        } catch (RepositoryException e) {
            assertTrue(e.getCause() instanceof SparqlRuleException);
        }
    }

    @Test
    public void testNoStringRules() {
        try(OntopRepositoryConnection ignored = H2RDF4JTestTools.initOBDA(JDBC_URL, OBDA_FILE, null, null, null, null,
                "/employee/rejected/non-string-rules.toml")) {
            fail("A repository exception was expected");
        } catch (RepositoryException e) {
            assertTrue(e.getCause() instanceof SparqlRuleException);
        }
    }


}
