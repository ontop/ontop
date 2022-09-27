package it.unibz.inf.ontop.docker.postgres.nested;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
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

public abstract class AbstractHrJsonTest extends AbstractVirtualModeTest {

    Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testFullNames() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?v " +
                "WHERE {" +
                "?person  a :Person ; \n" +
                "         :fullName ?v . " +
                "}";
        ImmutableList<String> expectedValues =
                ImmutableList.of("Bob Loblaw", "Kenny McCormick", "Mary Poppins", "Roger Rabbit");

        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

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
                ImmutableList.of( "111", "111", "222", "222", "333");

        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Ignore("Limitation from the json datatype (cannot apply distinct on it). No problem with jsonb")
    @Test
    public void testFlattenTags2() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?person ?tagIds ?v " +
                "WHERE {" +
                "?person  :tag_ids ?tagIds . " +
                "?person  :tag_str ?v . " +
                "}";
        ImmutableList<String> expectedValues =
                ImmutableList.of( "111", "111", "222", "222", "333");

        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testTagIds() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?person ?v " +
                "WHERE {" +
                "?person  :tag_ids ?v . " +
                "}";
        ImmutableList<String> expectedValues =
                ImmutableList.of( "[111, 222, 333]", "[111, 222]", "[]");

        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testTagsAndIds() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?person ?v " +
                "WHERE {" +
                "?person  :tag_ids ?v . " +
                "?person  :tag_str ?s . " +
                "}";
        ImmutableList<String> expectedValues =
                ImmutableList.of( "[111, 222, 333]", "[111, 222]");

        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testFlattenFriends() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?person ?f ?v " +
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
