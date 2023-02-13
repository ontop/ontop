package it.unibz.inf.ontop.docker.lightweight.duckdb;

import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.AthenaLightweightTest;
import it.unibz.inf.ontop.docker.lightweight.DuckDBLightweightTest;
import org.junit.jupiter.api.Disabled;

@Disabled("Athena does not currently support integrity constraints. Furthermore, the test cases don't work, because" +
        "default schemas are not supported for Athena, which is required by the base behaviour of the test cases.")
@DuckDBLightweightTest
public class ConstraintDuckDBTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-athena.properties";

    public ConstraintDuckDBTest(String method) {
        super(method, PROPERTIES_FILE);
    }

}
