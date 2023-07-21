package it.unibz.inf.ontop.docker.postgres.nested;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHrJsonTest extends AbstractVirtualModeTest {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testFullNames() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?v " +
                "WHERE {" +
                "?person  a :Person ; \n" +
                "         :fullName ?v . " +
                "}";
        ImmutableList<String> expectedValues = getExpectedValuesFullNames();

        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

    }

    protected ImmutableList<String> getExpectedValuesFullNames() {
        return ImmutableList.of("Bob Loblaw", "Kenny McCormick", "Mary Poppins", "Roger Rabbit");
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
    public void testFlattenTags3() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?person ?tagIds ?v " +
                "WHERE {" +
                "BIND ('333' AS ?v)\n" +
                "?person  :tag_ids ?tagIds . " +
                "?person  :tag_str ?v . \n" +
                "}";
        ImmutableList<String> expectedValues =
                ImmutableList.of("333");

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
        ImmutableList<String> expectedValues = getExpectedValuesTagIds();

        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    protected ImmutableList<String> getExpectedValuesTagIds() {
        return ImmutableList.of( "[111, 222, 333]", "[111, 222]", "[]");
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
                ImmutableList.of( "[111, 222, 333]","[111, 222, 333]","[111, 222, 333]","[111, 222]", "[111, 222]");

        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);
    }

    @Test
    public void testFlattenFriends1() throws Exception {
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

    @Test
    public void testFlattenFriends2() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?person ?f ?v " +
                "WHERE {" +
                "?person  :hasFriend ?f . " +
                "?f  :city ?v .\n" +
                "FILTER (?f = <http://person.example.org/person/1/1>)\n" +
                "}";
        ImmutableList<String> expectedValues =
                ImmutableList.of( "Bolzano");

        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

    }
}
