package it.unibz.inf.ontop.docker.lightweight.cdatadynamodb;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.docker.lightweight.AbstractLeftJoinProfTest;
import it.unibz.inf.ontop.docker.lightweight.CDataDynamoDBLightweightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

@CDataDynamoDBLightweightTest
public class LeftJoinProfCDataDynamoDBTest extends AbstractLeftJoinProfTest {

    private static final String PROPERTIES_FILE = "/prof/cdatadynamodb/prof-cdatadynamodb.properties";
    private static final String OBDA_FILE = "/prof/cdatadynamodb/prof-cdatadynamodb.obda";


    @BeforeAll
    public static void before() throws IOException, SQLException {
        initOBDA(OBDA_FILE, OWL_FILE, PROPERTIES_FILE);
    }

    @AfterAll
    public static void after() throws SQLException {
        release();
    }

}
