package it.unibz.inf.ontop.docker.lightweight.dremio;

import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.DremioLightweightTest;
import org.junit.jupiter.api.Disabled;

@Disabled("Dremio does not support integrity constraints on postgresql data sources.")
@DremioLightweightTest
public class ConstraintDremioArrowFlightTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-dremio-arrowflight.properties";

    public ConstraintDremioArrowFlightTest() {
        super(PROPERTIES_FILE);
    }

}
