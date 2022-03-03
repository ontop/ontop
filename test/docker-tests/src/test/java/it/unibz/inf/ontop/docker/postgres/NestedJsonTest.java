package it.unibz.inf.ontop.docker.postgres;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class NestedJsonTest extends AbstractVirtualModeTest {

    Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    final static String owlFile = "/pgsql/nested/hr_person.owl";
    final static String obdaFile = "/pgsql/nested/hr_person.obda";
    final static String propertyFile = "/pgsql/nested/hr_person.properties";
    final static String viewFile = "/pgsql/nested/hr_person_views.json";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasonerWithViews(owlFile, obdaFile, propertyFile, viewFile);
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
    public void testFlattenTags() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?person ?ssn ?v " +
                "WHERE {" +
                "?person  :ssn ?ssn . " +
                "?person  :tag_str ?v . " +
                "}";
        ImmutableList<String> expectedValues =
                ImmutableList.of( "111", "222", "333" );

        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

    }

    @Test
    public void testFlattenFriends() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?person ?v " +
                "WHERE {" +
                "?person  :hasFriend ?f . " +
                "?f  :city ?v ." +
                "}";
        ImmutableList<String> expectedValues =
                ImmutableList.of( "Bolzano", "Merano");

        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

    }
}
