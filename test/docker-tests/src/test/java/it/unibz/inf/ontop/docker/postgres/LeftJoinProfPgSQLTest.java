package it.unibz.inf.ontop.docker.postgres;

import it.unibz.inf.ontop.docker.AbstractLeftJoinProfTest;

public class LeftJoinProfPgSQLTest extends AbstractLeftJoinProfTest {

    private static final String owlFileName = "/redundant_join/redundant_join_fk_test.owl";
    private static final String obdaFileName = "/redundant_join/redundant_join_fk_test.obda";
    private static final String propertyFileName = "/pgsql/redundant_join_fk_test.properties";

    public LeftJoinProfPgSQLTest() {
        super(owlFileName, obdaFileName, propertyFileName);
    }
}
