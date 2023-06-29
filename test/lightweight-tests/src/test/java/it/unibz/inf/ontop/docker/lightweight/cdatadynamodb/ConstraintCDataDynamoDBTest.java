package it.unibz.inf.ontop.docker.lightweight.cdatadynamodb;

import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.CDataDynamoDBLightweightTest;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

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
        super(method, PROPERTIES_FILE);
    }

    @Test
    @Disabled("We only support non-composite PRIMARY KEYS for DynamoDB at this moment.")
    @Override
    public void testPrimaryKey2() {
        super.testPrimaryKey2();
    }

    @Test
    @Disabled("We only support non-composite PRIMARY KEYS for DynamoDB at this moment.")
    @Override
    public void testPrimaryKey3() {
        super.testPrimaryKey3();
    }

    @Test
    @Disabled("We only support non-composite PRIMARY KEYS for DynamoDB at this moment.")
    @Override
    public void testPrimaryKey4() {
        super.testPrimaryKey4();
    }

    @Test
    @Disabled("We only support non-composite PRIMARY KEYS for DynamoDB at this moment.")
    @Override
    public void testForeignKey1() {
        super.testForeignKey1();
    }

    @Test
    @Disabled("We only support non-composite PRIMARY KEYS for DynamoDB at this moment.")
    @Override
    public void testForeignKey2() {
        super.testForeignKey2();
    }

    @Test
    @Disabled("We only support non-composite PRIMARY KEYS for DynamoDB at this moment.")
    @Override
    public void testForeignKey3() {
        super.testForeignKey3();
    }

    @Test
    @Disabled("We only support non-composite PRIMARY KEYS for DynamoDB at this moment.")
    @Override
    public void testForeignKey4() {
        super.testForeignKey4();
    }
}
