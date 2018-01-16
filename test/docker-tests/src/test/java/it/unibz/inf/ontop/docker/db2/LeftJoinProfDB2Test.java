package it.unibz.inf.ontop.docker.db2;

import it.unibz.inf.ontop.docker.AbstractLeftJoinProfTest;


public class LeftJoinProfDB2Test extends AbstractLeftJoinProfTest {
    private static final String owlFileName = "/redundant_join/redundant_join_fk_test.owl";
    private static final String obdaFileName = "/redundant_join/redundant_join_fk_test.obda";
    private static final String propertyFileName = "/db2/redundant_join_fk_test.properties";


    public LeftJoinProfDB2Test() {
        super(owlFileName, obdaFileName, propertyFileName);
    }

}