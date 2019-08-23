package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractLeftJoinProfTest;

public class LeftJoinProfMySQLTest extends AbstractLeftJoinProfTest {


    private static final String owlFileName = "/redundant_join/redundant_join_fk_test.owl";
    private static final String obdaFileName = "/redundant_join/redundant_join_fk_test.obda";
    private static final String propertyFileName = "/mysql/redundant_join_fk_test.properties";


    public LeftJoinProfMySQLTest() {
        super(owlFileName, obdaFileName, propertyFileName);
    }

}
