package it.unibz.inf.ontop.docker.lightweight.bigquery;

import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.BigQueryLightweightTest;
import org.junit.jupiter.api.Disabled;

@Disabled("BigQuery does not support integrity constraints")
@BigQueryLightweightTest
public class ConstraintBigQueryTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-bigquery.properties";

    public ConstraintBigQueryTest() {
        super(PROPERTIES_FILE);
    }

}
