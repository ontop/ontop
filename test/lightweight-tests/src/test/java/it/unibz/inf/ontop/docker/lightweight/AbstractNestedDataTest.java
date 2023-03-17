package it.unibz.inf.ontop.docker.lightweight;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/***
 * Class to test if nested data structure functionality is supported correctly.
 */
public abstract class AbstractNestedDataTest extends AbstractDockerRDF4JTest {

    protected static final String OBDA_FILE = "/nested/nested.obda";
    protected static final String OWL_FILE = "/nested/nested.owl";

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

        ImmutableSet<String> expectedValues = getFullNamesExpectedValues();
        executeAndCompareValues(query, expectedValues);
    }

    protected ImmutableSet<String> getFullNamesExpectedValues() {
        return ImmutableSet.of("\"Bob Loblaw\"^^xsd:string", "\"Kenny McCormick\"^^xsd:string", "\"Mary Poppins\"^^xsd:string", "\"Roger Rabbit\"^^xsd:string");
    }

    @Test
    public void testFlattenTags() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?person ?ssn ?v " +
                "WHERE {" +
                "?person  :ssn ?ssn . " +
                "?person  :tag_str ?v . " +
                "} ORDER BY ?v";

        executeAndCompareValues(query, getFlattenTagsExpectedValues());
    }

    protected ImmutableMultiset getFlattenTagsExpectedValues() {
        return ImmutableMultiset.of( "\"111\"^^xsd:string", "\"111\"^^xsd:string", "\"222\"^^xsd:string", "\"222\"^^xsd:string", "\"333\"^^xsd:string");
    }

    @Test
    public void testFlattenTags2() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?person ?tagIds ?v " +
                "WHERE {" +
                "?person  :tag_ids ?tagIds . " +
                "?person  :tag_str ?v . " +
                "} ORDER BY ?v";

        executeAndCompareValues(query, getFlattenTags2ExpectedValues());
    }

    protected ImmutableMultiset getFlattenTags2ExpectedValues() {
        return ImmutableMultiset.of( "\"111\"^^xsd:string", "\"111\"^^xsd:string", "\"222\"^^xsd:string", "\"222\"^^xsd:string", "\"333\"^^xsd:string");
    }

    @Test
    public void testTagIds() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?person ?v " +
                "WHERE {" +
                "?person  :tag_ids ?v . " +
                "}";

        ImmutableSet<String> expectedValues = getTagIdsExpectedValues();
        executeAndCompareValues(query, expectedValues);
    }

    protected ImmutableSet<String> getTagIdsExpectedValues() {
        return ImmutableSet.of( "[111, 222, 333]", "[111, 222]", "[]");
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

        executeAndCompareValues(query, getTagsAndIdsExpectedValues());
    }

    protected ImmutableMultiset getTagsAndIdsExpectedValues() {
        return ImmutableMultiset.of( "\"[111, 222, 333]\"^^xsd:string", "\"[111, 222, 333]\"^^xsd:string",
                "\"[111, 222, 333]\"^^xsd:string", "\"[111, 222]\"^^xsd:string", "\"[111, 222]\"^^xsd:string");
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

        executeAndCompareValues(query, getFlattenFriendsExpectedValues());
    }

    protected ImmutableMultiset getFlattenFriendsExpectedValues() {
        return ImmutableMultiset.of( "\"Bolzano\"^^xsd:string", "\"Merano\"^^xsd:string");
    }

    @Test
    public void testAllFriends() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?v " +
                "WHERE {" +
                "?x a :Friend . " +
                "?x :firstName ?v ." +
                "}";

        executeAndCompareValues(query, getAllFriendsExpectedValues());

    }

    protected ImmutableMultiset getAllFriendsExpectedValues() {
        return ImmutableMultiset.of( "\"Alice\"^^xsd:string", "\"Robert\"^^xsd:string");
    }

    @Test
    public void testSelfJoinElimination() throws Exception {
        String query = "PREFIX : <http://pub.example.org/>" +
                "\n" +
                "SELECT (CONCAT(?name, \": \", ?title) as ?v) " +
                "WHERE {" +
                "?person :name ?name . " +
                "?person :author ?pub . " +
                "?pub :title ?title . " +
                "}";

        executeAndCompareValues(query, getSelfJoinEliminationExpectedValues());

        /*checkContainsAllSetSemanticsWithErrorMessage(
                query,
                ImmutableSet.of(
                        ImmutableMap.of("person", "<http://pub.example.org/person/1>", "name", "Sanjay Ghemawat", "title", "The Google file system"),
                        ImmutableMap.of("person", "<http://pub.example.org/person/1>", "name", "Sanjay Ghemawat", "title", "MapReduce: Simplified Data Processing on Large Clusters"),
                        ImmutableMap.of("person", "<http://pub.example.org/person/1>", "name", "Sanjay Ghemawat", "title", "Bigtable: A Distributed Storage System for Structured Data"),
                        ImmutableMap.of("person", "<http://pub.example.org/person/2>", "name", "Jeffrey Dean", "title", "Bigtable: A Distributed Storage System for Structured Data"),
                        ImmutableMap.of("person", "<http://pub.example.org/person/2>", "name", "Jeffrey Dean", "title", "MapReduce: Simplified Data Processing on Large Clusters")
                ));*/

    }

    protected ImmutableMultiset<String> getSelfJoinEliminationExpectedValues() {
        return ImmutableMultiset.of("\"Sanjay Ghemawat: The Google file system\"^^xsd:string",
                "\"Sanjay Ghemawat: MapReduce: Simplified Data Processing on Large Clusters\"^^xsd:string",
                "\"Sanjay Ghemawat: Bigtable: A Distributed Storage System for Structured Data\"^^xsd:string",
                "\"Jeffrey Dean: Bigtable: A Distributed Storage System for Structured Data\"^^xsd:string",
                "\"Jeffrey Dean: MapReduce: Simplified Data Processing on Large Clusters\"^^xsd:string",
                "\"Jeffrey Dean: Large Scale Distributed Deep Networks\"^^xsd:string");
    }

    @Test
    public void testSPO() {
        String query = "PREFIX : <http://pub.example.org/>\n" + "SELECT * WHERE { ?s ?p ?o. }";
        assertEquals(getSPOExpectedCount(), runQueryAndCount(query));
    }

    protected int getSPOExpectedCount() {
        return 56;
    }

}
