package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class BasicViewWithConstraintsProfWithDuplicatesTest {
    private static final String VIEW_FILE = "src/test/resources/prof/prof-basic-views-with-constraints-duplicateUCFD.json";
    private static final String DBMETADATA_FILE = "src/test/resources/prof/prof_with_constraints.db-extract.json";

    ImmutableSet<OntopViewDefinition> viewDefinitions = ViewDefinitionParsingTest.loadViewDefinitions(VIEW_FILE, DBMETADATA_FILE);

    public BasicViewWithConstraintsProfWithDuplicatesTest() throws Exception {
    }

    /**
     * Duplicate functional dependencies appear only once - check determinant
     */
    @Test
    public void testProfDuplicateFDDeterminants() throws Exception {
        ImmutableSet<String> otherFD = viewDefinitions.stream()
                .map(RelationDefinition::getOtherFunctionalDependencies)
                .flatMap(Collection::stream)
                .map(FunctionalDependency::getDeterminants)
                .flatMap(Collection::stream)
                .map(d -> d.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("first_name"), otherFD);
    }

    /**
     * Duplicate functional dependencies appear only once - check dependent
     */
    @Test
    public void testProfDuplicateFDDependents() throws Exception {
        ImmutableSet<String> otherFD = viewDefinitions.stream()
                .map(RelationDefinition::getOtherFunctionalDependencies)
                .flatMap(Collection::stream)
                .map(FunctionalDependency::getDependents)
                .flatMap(Collection::stream)
                .map(d -> d.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("last_name"), otherFD);
    }

    /**
     * Duplicate unique constraints appear only once - check determinant
     */
    @Test
    public void testProfDuplicateUCColumn() throws Exception {
        ImmutableSet<String> constraints = viewDefinitions.stream()
                .map(RelationDefinition::getUniqueConstraints)
                .flatMap(Collection::stream)
                .map(UniqueConstraint::getAttributes)
                .flatMap(Collection::stream)
                .map(d -> d.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("position", "a_id"), constraints);
    }
}
