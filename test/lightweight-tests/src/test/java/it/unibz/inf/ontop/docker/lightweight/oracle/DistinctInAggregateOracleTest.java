package it.unibz.inf.ontop.docker.lightweight.oracle;

import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.OracleLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@OracleLightweightTest
public class DistinctInAggregateOracleTest extends AbstractDistinctInAggregateTest {

    private static final String PROPERTIES_FILE = "/university/university-oracle.properties";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
    }

    @Disabled
    @Test
    public void testAvgDistinct() throws Exception {
        super.testAvgDistinct();
    }

    @Disabled("Distinct statistical functions are not supported by this dialect.")
    @Test
    @Override
    public void testStdevDistinct() throws Exception {
        super.testStdevDistinct();
    }

    @Disabled("Distinct statistical functions are not supported by this dialect.")
    @Test
    @Override
    public void testVarianceDistinct() throws Exception {
        super.testVarianceDistinct();
    }
}
