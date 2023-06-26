package it.unibz.inf.ontop.docker.lightweight.dremio;

import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.docker.lightweight.AbstractConstraintTest;
import it.unibz.inf.ontop.docker.lightweight.DremioLightweightTest;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.sql.SQLException;

@Disabled("Dremio does not support integrity constraints on postgresql data sources.")
@DremioLightweightTest
public class ConstraintDremioTest extends AbstractConstraintTest {

    private static final String PROPERTIES_FILE = "/dbconstraints/dbconstraints-dremio.properties";

    public ConstraintDremioTest(String method) {
        super(method, PROPERTIES_FILE);
    }

}
