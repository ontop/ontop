package it.unibz.inf.ontop.docker.lightweight.mysql;

import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.MySQLLightweightTest;
import org.junit.jupiter.api.BeforeAll;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

@MySQLLightweightTest
public class DistinctInAggregateMySQLTest extends AbstractDistinctInAggregateTest {

    private static String propertiesFile = "/university/university-mysql.properties";

    @BeforeAll
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertiesFile);
        CONNECTION = REASONER.getConnection();
    }
}
