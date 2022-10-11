package it.unibz.inf.ontop.docker.lightweight.oracle;

import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class DistinctInAggregateOracleTest extends AbstractDistinctInAggregateTest {

    private static String propertiesFile = "/university/university-oracle.properties";

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertiesFile);
        CONNECTION = REASONER.getConnection();
    }

    @Ignore
    @Test
    public void testAvgDistinct() throws Exception {
        super.testAvgDistinct();
    }
}
