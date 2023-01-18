package it.unibz.inf.ontop.docker.mssql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.AbstractBindTestWithFunctions;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */
public class BindWithFunctionsSqlServerTest extends AbstractBindTestWithFunctions {
    private static final String owlfile = "/mssql/sparqlBind.owl";
    private static final String obdafile = "/mssql/sparqlBindSqlServer.obda";
    private static final String propertiesfile = "/mssql/sparqlBindSqlServer.properties";

    @BeforeClass
    public static void before() {
        CONNECTION = createReasoner(owlfile, obdafile, propertiesfile);
    }

    @Override
    protected List<String> getAbsExpectedValues() {
        return ImmutableList.of(
                "\"8.500000\"^^xsd:decimal",
                "\"5.750000\"^^xsd:decimal",
                "\"6.700000\"^^xsd:decimal",
                "\"1.500000\"^^xsd:decimal");
    }

    @Override
    protected List<String> getRoundExpectedValues() {
        return ImmutableList.of(
                "\"0E-19, 43.0000000000000000000\"^^xsd:string",
                "\"0E-19, 23.0000000000000000000\"^^xsd:string",
                "\"0E-19, 34.0000000000000000000\"^^xsd:string",
                "\"0E-19, 10.0000000000000000000\"^^xsd:string");
    }

    @Override
    protected List<String> getStrExpectedValues() {
        return ImmutableList.of(
                "\"1967-11-05T07:50:00\"^^xsd:string",
                "\"2011-12-08T12:30:00\"^^xsd:string",
                "\"2014-06-05T18:47:52\"^^xsd:string",
                "\"2015-09-21T09:23:06\"^^xsd:string");
    }

    @Ignore("DATETIME does not have an offset. TODO: update the data source (use DATETIME2 instead)")
    @Test
    public void testTZ() throws Exception {
        super.testTZ();
    }

    @Ignore("not supported?")
    @Test
    public void testREGEX() throws Exception {
        super.testREGEX();
    }

    @Ignore("not supported")
    @Test
    public void testREPLACE() throws Exception {
        super.testREPLACE();
    }

    @Override
    protected List<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of(
                "\"0.500000\"^^xsd:decimal");
    }

    @Test
    @Ignore("TODO: support regex")
    @Override
    public void testIRI7() throws Exception {
        super.testIRI7();
    }

    /**
     * SQL Server different input file.
     */
    @Override
    protected List<String> getDaysDTExpectedValuesMappingInput() {
        return ImmutableList.of(
                "\"16360\"^^xsd:long",
                "\"17270\"^^xsd:long",
                "\"17742\"^^xsd:long",
                "\"255\"^^xsd:long");
    }

    /**
     * SQL Server different input file.
     */
    @Override
    protected List<String> getSecondsExpectedValuesMappingInput() {
        return ImmutableList.of(
                "\"1413514800\"^^xsd:long",
                "\"1492161472\"^^xsd:long",
                "\"1532994786\"^^xsd:long",
                "\"22112400\"^^xsd:long");
    }

    @Ignore("Current MS SQL Server handling does not allow operation between DATE and DATETIME, db example has only DATE")
    @Test
    @Override
    public void testDaysBetweenDateMappingInput() throws Exception {
        super.testDaysBetweenDateMappingInput();
    }
}
