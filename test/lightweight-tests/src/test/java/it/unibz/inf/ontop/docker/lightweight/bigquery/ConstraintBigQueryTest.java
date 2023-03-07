package it.unibz.inf.ontop.docker.lightweight.bigquery;

import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.BigQueryLightweightTest;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.sql.SQLException;

@Disabled("BigQuery does not support integrity constraints")
@BigQueryLightweightTest
public class ConstraintBigQueryTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-bigquery.properties";

    public ConstraintBigQueryTest(String method) {
        super(method, PROPERTIES_FILE);
    }

}
