package it.unibz.inf.ontop.docker.postgres.nested;

import com.google.common.collect.ImmutableList;
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

public class DenormalizedHrJsonTest extends AbstractVirtualModeTest {

    Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    final static String owlFile = "/pgsql/nested/hr/hr.owl";
    final static String obdaFile = "/pgsql/nested/hr/hr-denormalized.obda";
    final static String propertyFile = "/pgsql/nested/hr/hr.properties";
    final static String viewFile = "/pgsql/nested/hr/hr_lenses.json";

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
    public void testFullNames() throws Exception {
        String query = "PREFIX : <http://person.example.org/>" +
                "\n" +
                "SELECT  ?v " +
                "WHERE {" +
                "?person  a :Person ; \n" +
                "         :fullName ?v . " +
                "}";
        ImmutableList<String> expectedValues =
                ImmutableList.of( "Mary Poppins", "Roger Rabbit");

        String sql = checkReturnedValuesUnorderedReturnSql(query, expectedValues);

        LOGGER.debug("SQL Query: \n" + sql);

    }

}
