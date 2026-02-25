package it.unibz.inf.ontop.docker.lightweight.duckdb;

import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.DuckDBLightweightTest;

@DuckDBLightweightTest
public class ConstraintDuckDBTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-duckdb.properties";

    public ConstraintDuckDBTest() {
        super(PROPERTIES_FILE);
    }

}
