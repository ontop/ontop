package it.unibz.inf.ontop.docker.lightweight.mysql;

import it.unibz.inf.ontop.docker.lightweight.AbstractDistinctInAggregateTest;
import it.unibz.inf.ontop.docker.lightweight.MySQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@MySQLLightweightTest
public class DistinctInAggregateMySQLTest extends AbstractDistinctInAggregateTest {

    private static final String PROPERTIES_FILE = "/university/university-mysql.properties";

    @BeforeAll
    public static void before() {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() {
        release();
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
