package it.unibz.inf.ontop.docker;

import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.GraphOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

public class Issue381 extends AbstractVirtualModeTest {
    private static final String owlFile = "/issue381/ontology.owl";
    private static final String obdaFile = "/issue381/mapping.obda";
    private static final String propertyFile = "/issue381/mapping.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException, SQLException {
        Connection sqlConnection = DriverManager.getConnection("jdbc:h2:mem:questjunitdb", "sa", "");
        try (java.sql.Statement s = sqlConnection.createStatement()) {
            String text = "CREATE TABLE \"t11\" (\"s\" varchar(255) NOT NULL, \"o\" varchar(255) NOT NULL);\n" +
                    "CREATE TABLE \"t12\" (\"s\" varchar(255) NOT NULL, \"o\" varchar(255) NOT NULL, \"l\" varchar(10));\n" +
                    "CREATE TABLE \"t2\" (\"s\" varchar(255) NOT NULL, \"o\" varchar(255) NOT NULL);\n" +
                    "CREATE TABLE \"t3\" (\"s\" varchar(255) NOT NULL, \"o\" LONG NOT NULL)";
            s.execute(text);
        } catch (SQLException e) {
            System.out.println("Exception in creating db from script:" + e);
        }


        REASONER = createReasoner(owlFile, obdaFile, propertyFile);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    @Test
    public void testLoad() throws Exception {
        String sparqlQuery =
                "SELECT  *\n" +
                        "WHERE\n" +
                        "  { ?s  <http://ex.org/p1>  ?o ;\n" +
                        "        <http://ex.org/p2>  ?o1 ;\n" +
                        "        ?p                  ?o\n" +
                        "  }";

        try (OWLStatement st = createStatement(); TupleOWLResultSet rs = st.executeSelectQuery(sparqlQuery)) {
            assertTrue(rs.hasNext());
        }
    }

}
