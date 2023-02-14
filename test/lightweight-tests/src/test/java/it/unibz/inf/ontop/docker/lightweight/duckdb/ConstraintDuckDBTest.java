package it.unibz.inf.ontop.docker.lightweight.duckdb;

import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.DuckDBLightweightTest;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.sql.SQLException;

@DuckDBLightweightTest
public class ConstraintDuckDBTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-duckdb.properties";

    public ConstraintDuckDBTest(String method) {
        super(method, PROPERTIES_FILE);
    }

    @Override
    public void setUp() throws IOException, SQLException, MetadataExtractionException {
        super.setUp();
    }

}
