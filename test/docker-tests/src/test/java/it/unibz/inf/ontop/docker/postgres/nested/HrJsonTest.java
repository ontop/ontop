package it.unibz.inf.ontop.docker.postgres.nested;

import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.semanticweb.owlapi.model.OWLException;

public class HrJsonTest extends AbstractHrJsonTest {

    private final static String owlFile = "/pgsql/nested/hr/hr.owl";
    private final static String obdaFile = "/pgsql/nested/hr/hr.obda";
    private final static String propertyFile = "/pgsql/nested/hr/hr.properties";
    private final static String lensesFile = "/pgsql/nested/hr/hr_lenses_json.json";

    private static EngineConnection CONNECTION;

    @BeforeClass
    public static void before() {
        CONNECTION = createReasonerWithLenses(owlFile, obdaFile, propertyFile, lensesFile);
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws Exception {
        CONNECTION.close();
    }
}
