package it.unibz.inf.ontop.docker.lightweight.spark;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.docker.lightweight.AbstractNestedDataTest;
import it.unibz.inf.ontop.docker.lightweight.SparkSQLLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.sql.SQLException;

@SparkSQLLightweightTest
public class NestedDataSparkTest extends AbstractNestedDataTest {

    private static final String PROPERTIES_FILE = "/nested/spark/nested-spark.properties";
    private static final String OBDA_FILE = "/nested/spark/nested-spark.obda";
    private static final String LENS_FILE = "/nested/spark/nested-lenses.json";

    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE, LENS_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

    //In SPARK, calling EXPLODE on an empty array results in no entries.
    @Override
    protected ImmutableSet<String> getFullNamesExpectedValues() {
        return ImmutableSet.of("\"Mary Poppins\"^^xsd:string", "\"Roger Rabbit\"^^xsd:string");
    }

    //In SPARK, calling EXPLODE on an empty array results in no entries.
    @Override
    protected ImmutableSet<String> getTagIdsExpectedValues() {
        return ImmutableSet.of( "\"[111, 222, 333]\"^^xsd:string", "\"[111, 222]\"^^xsd:string");
    }

    //Because of the reasons above, there are fewer triples in SPARK.
    @Override
    protected int getSPOExpectedCount() {
        return 56;
    }
}
