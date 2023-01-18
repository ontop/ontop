package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class BasicViewWithRenamingTest {
    private static final String VIEW_FILE = "src/test/resources/prof/prof-basic-views-renaming.json";
    private static final String DBMETADATA_FILE = "src/test/resources/prof/prof_with_constraints.db-extract.json";

    private final ImmutableSet<Lens> viewDefinitions = LensParsingTest.loadLensesH2(VIEW_FILE, DBMETADATA_FILE);

    public BasicViewWithRenamingTest() throws Exception {
    }

    /**
     * Constraint inherited from parent besides renaming
     */
    @Test
    public void testProfUniqueConstraintOnHiddenColumns() {
        ImmutableSet<String> constraints = viewDefinitions.stream()
                .map(RelationDefinition::getUniqueConstraints)
                .flatMap(Collection::stream)
                .map(UniqueConstraint::getAttributes)
                .flatMap(Collection::stream)
                .map(v -> v.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("id"), constraints);
    }
}
