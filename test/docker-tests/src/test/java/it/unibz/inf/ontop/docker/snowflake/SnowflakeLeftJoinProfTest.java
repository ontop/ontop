package it.unibz.inf.ontop.docker.snowflake;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

@Ignore
public class SnowflakeLeftJoinProfTest extends AbstractLeftJoinProfTest {

    private static final String owlFileName = "/redundant_join/redundant_join_fk_test.owl";
    private static final String obdaFileName = "/snowflake/redundant_join/redundant_join_fk_test.obda";
    private static final String propertyFileName = "/snowflake/redundant_join/redundant_join_fk_test.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFileName, obdaFileName, propertyFileName);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return  ImmutableList.of("11.200000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return   ImmutableList.of("10.333333","12.000000", "13.000000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("0", "0", "0", "0", "0", "10.333333","12.000000", "13.000000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("0", "0", "0", "0", "0", "18.00000", "20.00000", "84.50000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedSum1() {
        return ImmutableList.of("31.00000", "32.00000", "115.50000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("15.50000000000", "16.00000000000", "19.25000000000");
    }
}
