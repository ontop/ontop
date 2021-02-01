package it.unibz.inf.ontop.docker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLStatement;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import it.unibz.inf.ontop.owlapi.resultset.OWLBinding;
import it.unibz.inf.ontop.owlapi.resultset.OWLBindingSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

public class Issue390 extends AbstractVirtualModeTest {

    private static final String owlFile = "/issue390/ontology.owl";
    private static final String obdaFile = "/issue390/mapping.ttl";
    private static final String propertyFile = "/issue390/mapping.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException, SQLException {

        Connection sqlConnection = DriverManager.getConnection("jdbc:h2:mem:questjunitdb", "sa", "");
        try (java.sql.Statement s = sqlConnection.createStatement()) {
            String text = new Scanner(new File("src/test/resources/issue390/database.sql")).useDelimiter("\\A").next();
            s.execute(text);
        }
        catch (SQLException | FileNotFoundException e) {
            System.out.println("Exception in creating db from script:" + e);
        }

        REASONER = createR2RMLReasoner(owlFile, obdaFile, propertyFile);
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
        ImmutableSet<ImmutableMap<String, String>> expected = ImmutableSet.of(
                ImmutableMap.of("g", "<http://example.org#graph_1>",
                        "s", "<http://example.org/agency/42>",
                        "p", "rdf:type",
                        "o", "<http://example.org/terms#agency>"),
                ImmutableMap.of("g", "<http://example.org#graph_1>",
                        "s", "<http://example.org/agency/42>",
                        "p", "<http://example.org#has_url>",
                        "o", "http://aaa.com"),
                ImmutableMap.of("g", "<http://example.org#graph_2>",
                        "s", "<http://example.org/agency/42>",
                        "p", "<http://example.org#has_url>",
                        "o", "http://aaa.com"));

        assertTrue(checkContainsTuplesSetSemantics("SELECT *\n" +
                "WHERE { GRAPH ?g { ?s ?p ?o } }", expected));
    }
}
