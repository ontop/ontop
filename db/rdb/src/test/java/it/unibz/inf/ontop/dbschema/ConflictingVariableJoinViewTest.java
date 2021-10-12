package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.impl.json.ConflictingVariableInJoinViewException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
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
        } catch (ConflictingVariableInJoinViewException e) {
            assertEquals(
                    e.getConflictingAttributeIds().stream().map(
                            id -> id.getAttribute().getName()).collect(ImmutableCollectors.toSet()),
                    ImmutableSet.of("c_id"));
            return;
        }
        fail();
    }
}
