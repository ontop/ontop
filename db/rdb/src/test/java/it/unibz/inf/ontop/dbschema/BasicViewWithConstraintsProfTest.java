package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class BasicViewWithConstraintsProfTest {
    private static final String VIEW_FILE = "src/test/resources/prof/prof-basic-views-with-constraints.json";
    private static final String DBMETADATA_FILE = "src/test/resources/prof/prof_with_constraints.db-extract.json";

    ImmutableSet<OntopViewDefinition> viewDefinitions = ViewDefinitionParsingTest.loadViewDefinitions(VIEW_FILE, DBMETADATA_FILE);

    public BasicViewWithConstraintsProfTest() throws Exception {
    }

    /**
     * Constraint involving a hidden column is not inherited from parent
     */
    @Test
    public void testProfUniqueConstraintOnHiddenColumns() throws Exception {
        ImmutableSet<String> constraints = viewDefinitions.stream()
                .map(RelationDefinition::getUniqueConstraints)
                .flatMap(Collection::stream)
                .map(UniqueConstraint::getAttributes)
                .flatMap(Collection::stream)
                .map(v -> v.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("position", "a_id"), constraints);
    }

}
