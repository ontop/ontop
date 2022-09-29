package it.unibz.inf.ontop.docker.postgres.nested;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class HrJsonDenormalizedTest extends AbstractHrJsonTest {

    final static String owlFile = "/pgsql/nested/hr/hr.owl";
    final static String obdaFile = "/pgsql/nested/hr/hr-denormalized.obda";
    final static String propertyFile = "/pgsql/nested/hr/hr.properties";
    final static String viewFile = "/pgsql/nested/hr/hr_lenses_jsonb.json";

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

    @Override
    protected ImmutableList<String> getExpectedValuesFullNames() {
        return ImmutableList.of("Mary Poppins", "Roger Rabbit");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesTagIds() {
        return ImmutableList.of( "[111, 222, 333]", "[111, 222]");
    }
}
