package it.unibz.inf.ontop.docker.postgres.nested;

import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class HrJsonTest extends AbstractHrJsonTest {

    final static String owlFile = "/pgsql/nested/hr/hr.owl";
    final static String obdaFile = "/pgsql/nested/hr/hr.obda";
    final static String propertyFile = "/pgsql/nested/hr/hr.properties";
    final static String lensesFile = "/pgsql/nested/hr/hr_lenses_json.json";

    private static OntopOWLEngine ENGINE;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        ENGINE = createReasonerWithLenses(owlFile, obdaFile, propertyFile, lensesFile);
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
}
