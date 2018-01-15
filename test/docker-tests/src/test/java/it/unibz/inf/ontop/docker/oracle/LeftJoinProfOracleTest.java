package it.unibz.inf.ontop.docker.oracle;

import it.unibz.inf.ontop.docker.AbstractLeftJoinProfTest;

public class LeftJoinProfOracleTest extends AbstractLeftJoinProfTest {


    private static final String owlFileName = "/redundant_join/redundant_join_fk_test.owl";
    private static final String obdaFileName = "/redundant_join/redundant_join_fk_test.obda";
    private static final String propertyFileName = "/oracle/oracle.properties";


    public LeftJoinProfOracleTest() {
        super(owlFileName, obdaFileName, propertyFileName);
    }

}
