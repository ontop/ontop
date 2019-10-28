package it.unibz.inf.ontop.docker.mssql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.AbstractLeftJoinProfTest;


public class LeftJoinProfMssqlTest extends AbstractLeftJoinProfTest {

    private static final String OWL_FILE = "/redundant_join/redundant_join_fk_test.owl";
    private static final String OBDA_FILE = "/redundant_join/redundant_join_fk_test.obda";
    private static final String PROPERTY_FILE = "/mssql/redundant_join_fk_test.properties";

    public LeftJoinProfMssqlTest() {
        super(OWL_FILE, OBDA_FILE, PROPERTY_FILE);
    }

    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("0", "0", "0", "0", "0", "18.000", "20.000", "54.500");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("15.500000", "16.000000", "18.875000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedSum1(){
        return ImmutableList.of("31.000", "32.000", "75.500");
    }
}


