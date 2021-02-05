package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractDistinctInAggregateTest;
import org.junit.BeforeClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class DistinctInAggregateMysqlTest extends AbstractDistinctInAggregateTest {

    private static String propertiesFile = "/mysql/university.properties";

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertiesFile);
        CONNECTION = REASONER.getConnection();
    }
}
