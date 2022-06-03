package it.unibz.inf.ontop.docker.postgres.nested;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
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
    public void testSelfJoinElimination() throws Exception {
        String query = "PREFIX : <http://pub.example.org/>" +
                "\n" +
                "SELECT  ?person ?name ?title" +
                "WHERE {" +
                "?person  :name ?name . " +
                "?person  :author ?pub . " +
                "?pub  :title ?title . " +
                "}";
        ImmutableList<String> expectedValues =
                ImmutableList.of( "111", "111", "222", "222", "333");

        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

    }
}
