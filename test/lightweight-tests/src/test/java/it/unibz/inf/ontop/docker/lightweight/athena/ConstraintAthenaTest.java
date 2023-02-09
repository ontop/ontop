package it.unibz.inf.ontop.docker.lightweight.athena;

import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.AthenaLightweightTest;
import org.junit.jupiter.api.Disabled;

@Disabled("Athena does not currently support integrity constraints.")
@AthenaLightweightTest
public class ConstraintAthenaTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-athena.properties";

    public ConstraintAthenaTest(String method) {
        super(method, PROPERTIES_FILE);
    }

}
