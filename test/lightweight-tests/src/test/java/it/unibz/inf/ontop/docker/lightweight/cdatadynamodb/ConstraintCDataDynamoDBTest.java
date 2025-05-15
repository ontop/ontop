package it.unibz.inf.ontop.docker.lightweight.cdatadynamodb;

import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.CDataDynamoDBLightweightTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * We only run the constraints test on the BOOK table. DynamoDB currently only supports primary keys as the column
 * that is chosen as "partition key". Therefore, it cannot be composite (except when an additional sort key is provided,
 * in which case the unique constraint will consist of (partition_key, sort_key).
 * This main functionality is already tested through the BOOK table, adding further table would cause additional costs with
 * no benefit.
 */
@CDataDynamoDBLightweightTest
public class ConstraintCDataDynamoDBTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-cdatadynamodb.properties";

    public ConstraintCDataDynamoDBTest(String method) {
        super(PROPERTIES_FILE);
    }

    @Test
    @Disabled("We only support non-composite PRIMARY KEYS for DynamoDB at this moment.")
    @Override
    public void testPrimaryKeyBookWriter() {
        super.testPrimaryKeyBookWriter();
    }

    @Test
    @Disabled("We only support non-composite PRIMARY KEYS for DynamoDB at this moment.")
    @Override
    public void testPrimaryKeyEdition() {
        super.testPrimaryKeyEdition();
    }

    @Test
    @Disabled("We only support non-composite PRIMARY KEYS for DynamoDB at this moment.")
    @Override
    public void testPrimaryKeyWriter() {
        super.testPrimaryKeyWriter();
    }

    @Test
    @Disabled("We only support non-composite PRIMARY KEYS for DynamoDB at this moment.")
    @Override
    public void testForeignKeyBook() {
        super.testForeignKeyBook();
    }

    @Test
    @Disabled("We only support non-composite PRIMARY KEYS for DynamoDB at this moment.")
    @Override
    public void testForeignKeyBookWriter() {
        super.testForeignKeyBookWriter();
    }

    @Test
    @Disabled("We only support non-composite PRIMARY KEYS for DynamoDB at this moment.")
    @Override
    public void testForeignKeyEdition() {
        super.testForeignKeyEdition();
    }

    @Test
    @Disabled("We only support non-composite PRIMARY KEYS for DynamoDB at this moment.")
    @Override
    public void testForeignKeyWriter() {
        super.testForeignKeyWriter();
    }
}
