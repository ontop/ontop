package it.unibz.inf.ontop.docker.mysql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.AbstractLeftJoinProfTest;

public class LeftJoinProfMySQLTest extends AbstractLeftJoinProfTest {


    private static final String owlFileName = "/redundant_join/redundant_join_fk_test.owl";
    private static final String obdaFileName = "/redundant_join/redundant_join_fk_test.obda";
    private static final String propertyFileName = "/mysql/redundant_join_fk_test.properties";


    public LeftJoinProfMySQLTest() {
        super(owlFileName, obdaFileName, propertyFileName);
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return  ImmutableList.of("11.5000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("0", "0", "0", "0", "0", "10.5000", "12.0000", "13.0000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return   ImmutableList.of("10.5000","12.0000", "13.0000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("15.500000000000000000000000000000", "16.000000000000000000000000000000", "18.875000000000000000000000000000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("0", "0", "0", "0", "0", "18.000", "20.000", "54.500");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedSum1(){
        return ImmutableList.of("31.000", "32.000", "75.500");
    }

}
