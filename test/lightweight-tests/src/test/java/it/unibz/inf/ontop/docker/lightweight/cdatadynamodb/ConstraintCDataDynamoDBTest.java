package it.unibz.inf.ontop.docker.lightweight.cdatadynamodb;

import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.CDataDynamoDBLightweightTest;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.sql.SQLException;

@CDataDynamoDBLightweightTest
public class ConstraintCDataDynamoDBTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-cdatadynamodb.properties";

    public ConstraintCDataDynamoDBTest(String method) {
        super(method, PROPERTIES_FILE);
    }

}
