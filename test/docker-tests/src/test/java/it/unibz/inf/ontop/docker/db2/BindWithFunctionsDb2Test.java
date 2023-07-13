package it.unibz.inf.ontop.docker.db2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.AbstractBindTestWithFunctions;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */
public class BindWithFunctionsDb2Test extends AbstractBindTestWithFunctions {
	private static final String owlfile = "/db2/bind/sparqlBind.owl";
	private static final String obdafile = "/db2/bind/sparqlBindDb2.obda";
    private static final String propertiesfile = "/db2/bind/db2-smallbooks.properties";

    @BeforeClass
    public static void before() {
        CONNECTION = createReasoner(owlfile, obdafile, propertiesfile);
    }


    @Ignore("Not yet supported")
    @Test
    @Override
    public void testHashSHA256() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testUuid() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testStrUuid() {
    }

    @Override
    protected List<String> getDivideExpectedValues() {
        return ImmutableList.of(
                "\"21.2500000000000000000000000\"^^xsd:decimal",
                "\"11.5000000000000000000000000\"^^xsd:decimal",
                "\"16.7500000000000000000000000\"^^xsd:decimal",
                "\"5.0000000000000000000000000\"^^xsd:decimal");
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testTZ() {
    }

    @Override
    protected List<String> getConstantIntegerDivideExpectedResults() {
        return ImmutableList.of(
                "\"0.50000000000000000000000000\"^^xsd:decimal");
    }


    @Override
    protected List<String> getAbsExpectedValues() {
        return ImmutableList.of(
                "\"8.5000\"^^xsd:decimal",
                "\"5.7500\"^^xsd:decimal",
                "\"6.7000\"^^xsd:decimal",
                "\"1.5000\"^^xsd:decimal");
    }

    @Override
    protected List<String> getRoundExpectedValues() {
        return ImmutableList.of(
                "\"0.00, 43.00\"^^xsd:string",
                "\"0.00, 23.00\"^^xsd:string",
                "\"0.00, 34.00\"^^xsd:string",
                "\"0.00, 10.00\"^^xsd:string");
    }

    @Override
    protected List<String> getYearExpectedValues() {
        return ImmutableList.of(
                "\"2014\"^^xsd:integer",
                "\"2011\"^^xsd:integer",
                "\"2015\"^^xsd:integer",
                "\"1970\"^^xsd:integer");
    }

    @Override
    protected List<String> getSecondsExpectedValues() {
        return ImmutableList.of(
                "\"52.000000\"^^xsd:decimal",
                "\"0.000000\"^^xsd:decimal",
                "\"6.000000\"^^xsd:decimal",
                "\"0.000000\"^^xsd:decimal");
    }

    @Override
    protected List<String> getDaysDTExpectedValuesMappingInput() {
        return ImmutableList.of(
                "\"16360\"^^xsd:long",
                "\"17270\"^^xsd:long",
                "\"17742\"^^xsd:long",
                "\"1351\"^^xsd:long");
    }

    @Override
    protected List<String> getDaysExpectedValuesMappingInput() {
        return ImmutableList.of(
                "\"16360\"^^xsd:long",
                "\"17270\"^^xsd:long",
                "\"17743\"^^xsd:long",
                "\"1352\"^^xsd:long");
    }

    @Override
    protected List<String> getSecondsExpectedValuesMappingInput() {
        return ImmutableList.of(
                "\"1413514800\"^^xsd:long",
                "\"1492161472\"^^xsd:long",
                "\"1532994786\"^^xsd:long",
                "\"116806800\"^^xsd:long");
    }
}
