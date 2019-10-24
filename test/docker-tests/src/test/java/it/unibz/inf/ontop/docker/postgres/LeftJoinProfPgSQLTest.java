package it.unibz.inf.ontop.docker.postgres;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.AbstractLeftJoinProfTest;

public class LeftJoinProfPgSQLTest extends AbstractLeftJoinProfTest {

    private static final String owlFileName = "/redundant_join/redundant_join_fk_test.owl";
    private static final String obdaFileName = "/redundant_join/redundant_join_fk_test.obda";
    private static final String propertyFileName = "/pgsql/redundant_join_fk_test.properties";

    public LeftJoinProfPgSQLTest() {
        super(owlFileName, obdaFileName, propertyFileName);
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("15.5000000000000000", "16.0000000000000000", "18.8750000000000000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return  ImmutableList.of("11.5000000000000000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return ImmutableList.of("10.5000000000000000","12.0000000000000000", "13.0000000000000000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("0", "0", "0", "0", "0", "10.5000000000000000", "12.0000000000000000", "13.0000000000000000");
    }

}
