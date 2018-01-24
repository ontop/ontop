package it.unibz.inf.ontop.docker.mssql;

import it.unibz.inf.ontop.docker.AbstractLeftJoinProfTest;


public class LeftJoinProfMssqlTest extends AbstractLeftJoinProfTest {

    private static final String OWL_FILE = "/redundant_join/redundant_join_fk_test.owl";
    private static final String OBDA_FILE = "/redundant_join/redundant_join_fk_test.obda";
    private static final String PROPERTY_FILE = "/mssql/redundant_join_fk_test.properties";

    public LeftJoinProfMssqlTest() {
        super(OWL_FILE, OBDA_FILE, PROPERTY_FILE);
    }
}


