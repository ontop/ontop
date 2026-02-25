package it.unibz.inf.ontop.docker.lightweight.presto;

import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.PrestoLightweightTest;
import org.junit.jupiter.api.Disabled;

@Disabled("Presto does not currently support integrity constraints.")
@PrestoLightweightTest
public class ConstraintPrestoTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-presto.properties";

    public ConstraintPrestoTest() {
        super(PROPERTIES_FILE);
    }

}
