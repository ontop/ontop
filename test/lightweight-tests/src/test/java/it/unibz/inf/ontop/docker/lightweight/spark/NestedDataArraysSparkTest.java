package it.unibz.inf.ontop.docker.lightweight.spark;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractNestedDataTest;
import it.unibz.inf.ontop.docker.lightweight.SparkSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@SparkSQLLightweightTest
public class NestedDataArraysSparkTest extends AbstractNestedDataTest {

    private static final String PROPERTIES_FILE = "/nested/spark/nested-spark.properties";
    private static final String OBDA_FILE = "/nested/nested.obda";
    private static final String LENS_FILE = "/nested/spark/nested-lenses-array.json";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE, LENS_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    @Override
    protected ImmutableMultiset getFlattenTimestampExpectedValues() {
        return ImmutableMultiset.of( "\"2023-01-01T18:00:00.000+00:00\"^^xsd:dateTime", "\"2023-01-15T18:00:00.000+00:00\"^^xsd:dateTime", "\"2023-01-29T12:00:00.000+00:00\"^^xsd:dateTime",
                "\"2023-02-12T18:00:00.000+00:00\"^^xsd:dateTime", "\"2023-02-26T18:00:00.000+00:00\"^^xsd:dateTime",
                "\"2023-03-12T18:00:00.000+00:00\"^^xsd:dateTime", "\"2023-03-26T18:00:00.000+00:00\"^^xsd:dateTime");
    }

    @Override
    protected ImmutableMultiset getFlattenWithAggregateExpectedValues() {
        return ImmutableMultiset.of("\"Carl: 15000.0\"^^xsd:string", "\"Jim: 15666.666666666666\"^^xsd:string",
                "\"Cynthia: 13000.0\"^^xsd:string", "\"Sam: 10000.0\"^^xsd:string",
                "\"Bob: 17666.666666666668\"^^xsd:string");
    }

    //Spark starts counting at 0
    @Override
    protected ImmutableMultiset getFlattenWithPositionExpectedValues() {
        return ImmutableMultiset.of( "\"1\"^^xsd:integer", "\"1\"^^xsd:integer", "\"1\"^^xsd:integer",
                "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"0\"^^xsd:integer", "\"2\"^^xsd:integer");
    }

    @Override
    protected ImmutableMultiset getFlattenIntegerExpectedValues() {
        return ImmutableMultiset.of( "\"10000\"^^xsd:int", "\"13000\"^^xsd:int", "\"18000\"^^xsd:int",
                "\"0\"^^xsd:int", "\"14000\"^^xsd:int", "\"15000\"^^xsd:int", "\"20000\"^^xsd:int");
    }

    @Override
    protected ImmutableMultiset getFlattenJsonPossiblyNullExpectedValues() {
        return ImmutableMultiset.of( "\"28\"^^xsd:int", "\"45\"^^xsd:int", "\"60\"^^xsd:int",
                "\"48\"^^xsd:int", "\"59\"^^xsd:int");
    }

}
