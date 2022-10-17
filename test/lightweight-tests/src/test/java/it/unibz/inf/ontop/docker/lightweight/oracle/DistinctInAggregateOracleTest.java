package it.unibz.inf.ontop.docker.lightweight.oracle;

import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.OracleLightweightTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

@OracleLightweightTest
public class DistinctInAggregateOracleTest extends AbstractDistinctInAggregateTest {

    private static String propertiesFile = "/university/university-oracle.properties";

    @BeforeAll
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertiesFile);
        CONNECTION = REASONER.getConnection();
    }

    @Disabled
    @Test
    public void testAvgDistinct() throws Exception {
        super.testAvgDistinct();
    }
}
