package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractDistinctInAggregateTest;
import org.junit.BeforeClass;

public class DistinctInAggregateMysqlTest extends AbstractDistinctInAggregateTest {

    private static final String propertiesFile = "/mysql/university.properties";

    @BeforeClass
    public static void before() {
        CONNECTION = createReasoner(owlFile, obdaFile, propertiesFile);
    }
}
