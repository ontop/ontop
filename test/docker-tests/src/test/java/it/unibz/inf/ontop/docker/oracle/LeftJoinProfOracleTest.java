package it.unibz.inf.ontop.docker.oracle;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.Arrays;
import java.util.List;

public class LeftJoinProfOracleTest extends AbstractLeftJoinProfTest {


    private static final String owlFileName = "/redundant_join/redundant_join_fk_test.owl";
    private static final String obdaFileName = "/redundant_join/redundant_join_fk_test.obda";
    private static final String propertyFileName = "/oracle/oracle.properties";

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

    /**
     * DISTINCT is not (yet ?) supported in LISTAGG in Oracle
     */
    @Ignore
    @Test
    @Override
    public void testGroupConcat3() throws Exception {
        super.testGroupConcat3();
    }

    /**
     * DISTINCT is not (yet ?) supported in LISTAGG in Oracle
     */
    @Ignore
    @Test
    @Override
    public void testGroupConcat5() throws Exception {
        super.testGroupConcat5();
    }

    /**
     * TODO: fix a bug of Oracle: using AVG in an ORDER BY condition may yield an incorrect order.
     * Fix: compute the AVG in a subquery, and use the output variable in the ORDER BY clause
     */
    @Ignore
    @Test
    @Override
    public void testAvgStudents3() throws Exception {
        super.testAvgStudents3();
    }

    /**
     * Oracle test DB has not been updated yet
     */
    @Override
    protected List<String> getExpectedValuesNonOptimizableLJAndJoinMix() {
        return ImmutableList.of("Depp", "Poppins", "Smith", "Smith");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return  ImmutableList.of("11.5");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return   ImmutableList.of("10.5","12", "13");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("0", "0", "0", "0", "0", "10.5", "12", "13");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("0", "0", "0", "0", "0", "18", "20", "54.5");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedSum1() {
        return ImmutableList.of("31", "32", "75.5");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("15.5", "16", "18.875");
    }

    @Override
    protected List<String> getExpectedValuesNicknameAndCourse() {
        return ImmutableList.of("Rog", "Rog", "Johnny");
    }

    @Override
    protected List<String> getExpectedValueSumStudents1() {
        return ImmutableList.of("46");
    }

    @Override
    protected List<String> getExpectedValueSumStudents2() {
        return ImmutableList.of("12", "13", "21");
    }

    @Override
    protected List<String> getExpectedValueSumStudents3() {
        return ImmutableList.of("0", "0", "0", "0", "0", "12", "13", "21");
    }

    @Override
    protected List<String> getExpectedValueSumStudents4() {
        return ImmutableList.of("John: 12", "Mary: 13", "Roger: 21");
    }

    @Override
    protected List<String> getExpectedValueSumStudents5() {
        return ImmutableList.of("John: 12", "Mary: 13", "Roger: 21");
    }

    @Override
    protected ImmutableList<String> getExpectedAggregationMappingProfStudentCountPropertyResults() {
        return ImmutableList.of("12", "13", "21");
    }
}
