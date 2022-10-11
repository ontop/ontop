package it.unibz.inf.ontop.docker.lightweight.mysql;

import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import org.junit.BeforeClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class DistinctInAggregateMySQLTest extends AbstractDistinctInAggregateTest {

    private static String propertiesFile = "/university/university-mysql.properties";

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertiesFile);
        CONNECTION = REASONER.getConnection();
    }
}
