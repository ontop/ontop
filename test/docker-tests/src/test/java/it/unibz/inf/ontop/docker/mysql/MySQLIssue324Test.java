package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;

import static org.junit.Assert.assertTrue;

/**
 * https://github.com/ontop/ontop/issues/324
 */
public class MySQLIssue324Test extends AbstractVirtualModeTest {

    static final String owlFile = "/mysql/issue324/ontology.ttl";
    static final String r2rmlFile = "/mysql/issue324/mapping.ttl";
    static final String propertyFile = "/mysql/issue324/issue324.properties";

    private static OntopOWLEngine REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() {
        REASONER = createR2RMLReasoner(owlFile, r2rmlFile, propertyFile);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
        REASONER.close();
    }

    private void runTests(String query1) throws Exception {
        try (OWLStatement st = createStatement()) {
            executeQueryAssertResults(query1, st);
        }
    }

    private void executeQueryAssertResults(String query, OWLStatement st) throws Exception {
        TupleOWLResultSet rs = st.executeSelectQuery(query);
        assertTrue(rs.hasNext());
        rs.close();
    }

    @Test
    public void testConcat() throws Exception {

        String query1 = "SELECT *\n" +
                "WHERE {\n" +
                "   ?s ?p ?o\n" +
                "} LIMIT 1";

        runTests(query1);
    }

}
