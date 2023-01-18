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
            LensParsingTest.loadLensesH2(VIEW_FILE, DBMETADATA_FILE);
            fail();
        }
        catch (ConflictingVariableInJoinViewException e) {
            assertEquals(ImmutableSet.of("c_id"),
                    e.getConflictingAttributeIds().stream()
                            .map(QuotedID::getName)
                            .collect(ImmutableCollectors.toSet()));
        }
    }
}
