package it.unibz.inf.ontop.docker.dremio;

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

public class DremioDefaultSchemaTest extends AbstractLeftJoinProfTest {
    private static final String owlFileName = "/redundant_join/redundant_join_fk_test.owl";
    private static final String obdaFileName = "/dremio/default_schema/redundant_join_fk_test.obda";
    private static final String propertyFileName = "/dremio/default_schema/redundant_join_fk_test.properties";
    private static final String constraintFileName = "/dremio/default_schema/keys.lst";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasonerWithConstraints(owlFileName, obdaFileName, propertyFileName, constraintFileName);
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
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return ImmutableList.of("10.333333333333334","12.0", "13.0");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("0", "0", "0", "0", "0", "10.333333333333334", "12.0",
                "13.0");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("0", "0", "0", "0", "0", "18.000000", "20.000000", "84.500000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedSum1(){
        return ImmutableList.of("31.000000000000000000000000000000", "32.000000000000000000000000000000", "115.500000000000000000000000000000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("15.5", "16.0", "19.25");
    }

    /**
     * GROUP_CONCAT or LIST_AGG not (yet ?) supported by Dremio
     */
    @Ignore
    @Test
    @Override
    public void testGroupConcat1() throws Exception {
        super.testGroupConcat1();
    }

    @Ignore
    @Test
    @Override
    public void testGroupConcat2() throws Exception {
        super.testGroupConcat2();
    }

    @Ignore
    @Test
    @Override
    public void testGroupConcat3() throws Exception {
        super.testGroupConcat3();
    }

    @Ignore
    @Test
    @Override
    public void testGroupConcat4() throws Exception {
        super.testGroupConcat4();
    }

    @Ignore
    @Test
    @Override
    public void testGroupConcat5() throws Exception {
        super.testGroupConcat5();
    }

    @Ignore
    @Test
    @Override
    public void testGroupConcat6() throws Exception {
        super.testGroupConcat6();
    }

    /**
     * Deactivated due to the following.
     * A condition like:
     * `CAST(CAST(<columnName> AS VARCHAR) AS BIGINT) = 3`
     * will generate a query over the source (i.e. Postgres) that contains:
     * `CAST(<columnName> AS VARCHAR(65536)) = 3`
     * And Postgres (rightfully) throws the exception:
     * ERROR: operator does not exist: character varying = integer
     */
    @Ignore
    @Test
    @Override
    public void testProperties() throws Exception {
        super.testProperties();
    }

}
