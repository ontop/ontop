package it.unibz.inf.ontop.docker.postgres.nested;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PubJsonTest extends AbstractVirtualModeTest {

    Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    final static String owlFile = "/pgsql/nested/pub/pub.owl";
    final static String obdaFile = "/pgsql/nested/pub/pub.obda";
    final static String propertyFile = "/pgsql/nested/pub/pub.properties";
    final static String viewFile = "/pgsql/nested/pub/pub_lenses.json";

    private static OntopOWLEngine ENGINE;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        ENGINE = createReasonerWithViews(owlFile, obdaFile, propertyFile, viewFile);
        CONNECTION = ENGINE.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
        ENGINE.close();
    }

    @Test
    public void testSelfJoinElimination() throws Exception {
        String query = "PREFIX : <http://pub.example.org/>" +
                "\n" +
                "SELECT ?person ?name ?title " +
                "WHERE {" +
                "?person :name ?name . " +
                "?person :author ?pub . " +
                "?pub :title ?title . " +
                "}";

        checkContainsAllSetSemanticsWithErrorMessage(
                query,
                ImmutableSet.of(
                        ImmutableMap.of("person", "<http://pub.example.org/person/1>", "name", "Sanjay Ghemawat", "title", "The Google file system"),
                        ImmutableMap.of("person", "<http://pub.example.org/person/1>", "name", "Sanjay Ghemawat", "title", "MapReduce: Simplified Data Processing on Large Clusters"),
                        ImmutableMap.of("person", "<http://pub.example.org/person/1>", "name", "Sanjay Ghemawat", "title", "Bigtable: A Distributed Storage System for Structured Data"),
                        ImmutableMap.of("person", "<http://pub.example.org/person/2>", "name", "Jeffrey Dean", "title", "Bigtable: A Distributed Storage System for Structured Data"),
                        ImmutableMap.of("person", "<http://pub.example.org/person/2>", "name", "Jeffrey Dean", "title", "MapReduce: Simplified Data Processing on Large Clusters")
                ));

    }
}
