package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.exception.MetadataExtractionException;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConflictingVariableJoinViewTest {
    private static final String VIEW_FILE = "src/test/resources/fake/join_views_conflicting_column1.json";
    private static final String DBMETADATA_FILE = "src/test/resources/fake/fake.db-extract.json";

    public ConflictingVariableJoinViewTest() {
    }

    @Test
    public void testPersonUniqueConstraint() throws Exception {
        try {
            ViewDefinitionParsingTest.loadViewDefinitions(VIEW_FILE, DBMETADATA_FILE);
        } catch (MetadataExtractionException e) {
            assertEquals(
                    "Conflict(s) detected: the following attribute(s) correspond(s) to multiple columns in the parent relations: [\"c_id\"]",
                    e.getMessage());
            return;
        }
        fail();
    }
}
