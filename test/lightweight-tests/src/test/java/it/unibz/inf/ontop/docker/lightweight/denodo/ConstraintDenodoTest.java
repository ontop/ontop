package it.unibz.inf.ontop.docker.lightweight.denodo;

import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.DenodoLightweightTest;
import it.unibz.inf.ontop.docker.lightweight.DuckDBLightweightTest;

@DenodoLightweightTest
public class ConstraintDenodoTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-denodo.properties";

    public ConstraintDenodoTest(String method) {
        super(method, PROPERTIES_FILE);
    }

}
