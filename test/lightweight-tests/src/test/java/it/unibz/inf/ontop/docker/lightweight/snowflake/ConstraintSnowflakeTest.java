package it.unibz.inf.ontop.docker.lightweight.snowflake;

import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.SnowflakeLightweightTest;

@SnowflakeLightweightTest
public class ConstraintSnowflakeTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-snowflake.properties";

    public ConstraintSnowflakeTest(String method) {
        super(method, PROPERTIES_FILE);
    }

}
