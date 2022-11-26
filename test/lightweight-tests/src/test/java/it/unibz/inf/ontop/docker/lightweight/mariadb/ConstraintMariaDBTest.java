package it.unibz.inf.ontop.docker.lightweight.mariadb;


import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.MariaDBLightweightTest;

@MariaDBLightweightTest
public class ConstraintMariaDBTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-mariadb.properties";

    public ConstraintMariaDBTest(String method) {
        super(method, PROPERTIES_FILE);
    }

}
