package it.unibz.inf.ontop.docker.lightweight.redshift;

import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.RedshiftLightweightTest;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.sql.SQLException;

@RedshiftLightweightTest
public class ConstraintRedshiftTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-redshift.properties";

    public ConstraintRedshiftTest(String method) {
        super(method, PROPERTIES_FILE);
    }

}
