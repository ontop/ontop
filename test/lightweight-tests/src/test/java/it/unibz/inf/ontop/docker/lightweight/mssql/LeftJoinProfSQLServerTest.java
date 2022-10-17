package it.unibz.inf.ontop.docker.lightweight.mssql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.owlapi.OntopOWLEngine;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


public class LeftJoinProfSQLServerTest extends AbstractLeftJoinProfTest {

    private static final String propertyFileName = "/prof/mssql/prof-mssql.properties";

    private static OntopOWLEngine REASONER;
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
    public static void after() throws Exception {
        CONNECTION.close();
        REASONER.close();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents1() {
        return  ImmutableList.of("11.200000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents2() {
        return ImmutableList.of("10.333333","12.000000", "13.000000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesAvgStudents3() {
        return ImmutableList.of("0", "0", "0", "0", "0", "10.333333", "12.000000", "13.000000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesDuration1() {
        return ImmutableList.of("0", "0", "0", "0", "0", "18.0000000000000000000", "20.0000000000000000000", "84.5000000000000000000");
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedAvg1() {
        return ImmutableList.of("15.5000000000000000000", "16.0000000000000000000", "19.2500000000000000000");
    }

    /**
     * DISTINCT is not (yet ?) supported in STRING_AGG
     */
    @Ignore
    @Test
    @Override
    public void testGroupConcat3() throws Exception {
        super.testGroupConcat3();
    }

    /**
     * DISTINCT is not (yet ?) supported in STRING_AGG
     */
    @Ignore
    @Test
    @Override
    public void testGroupConcat5() throws Exception {
        super.testGroupConcat5();
    }

    @Override
    protected ImmutableList<String> getExpectedValuesMultitypedSum1(){
        return ImmutableList.of("31.0000000000000000000", "32.0000000000000000000", "115.5000000000000000000");
    }
}


