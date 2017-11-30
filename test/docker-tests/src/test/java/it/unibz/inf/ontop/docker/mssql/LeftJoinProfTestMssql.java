package it.unibz.inf.ontop.docker.mssql;

import it.unibz.inf.ontop.docker.AbstractLeftJoinProfTest;
import org.junit.Ignore;

@Ignore("TODO: do it")
public class LeftJoinProfTestMssql extends AbstractLeftJoinProfTest {

    private static final String OWL_FILE = "src/test/resources/test/redundant_join/redundant_join_fk_test.owl";
    private static final String OBDA_FILE = "src/test/resources/test/redundant_join/redundant_join_fk_test.obda";
    private static final String PROPERTY_FILE = "src/test/resources/test/redundant_join/redundant_join_fk_test.properties";

    public LeftJoinProfTestMssql() {
        super(OWL_FILE, OBDA_FILE, PROPERTY_FILE);
    }
}


