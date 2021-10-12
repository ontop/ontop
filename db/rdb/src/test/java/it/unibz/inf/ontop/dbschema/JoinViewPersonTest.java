package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JoinViewPersonTest {
    private static final String VIEW_FILE = "src/test/resources/person/join_views.json";
    private static final String DBMETADATA_FILE = "src/test/resources/person/person_with_constraints.db-extract.json";

    ImmutableSet<OntopViewDefinition> viewDefinitions = ViewDefinitionParsingTest.loadViewDefinitions(VIEW_FILE, DBMETADATA_FILE);

    public JoinViewPersonTest() throws Exception {
    }

    @Test
    public void testPersonUniqueConstraint() {
        ImmutableSet<String> constraints = viewDefinitions.stream()
                .map(RelationDefinition::getUniqueConstraints)
                .flatMap(Collection::stream)
                .map(UniqueConstraint::getAttributes)
                .flatMap(Collection::stream)
                .map(v -> v.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("id", "id2"), constraints);
    }

    /**
     * Incomplete
     * TODO: enrich
     */
    @Test
    public void testPersonAddFunctionalDependencyDependent() {
        ImmutableSet<String> otherFD = viewDefinitions.stream()
                .map(RelationDefinition::getOtherFunctionalDependencies)
                .flatMap(Collection::stream)
                .map(FunctionalDependency::getDependents)
                .flatMap(Collection::stream)
                .map(d -> d.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("c_continent"), otherFD);
    }

    /**
     * Incomplete
     * TODO: enrich
     */
    @Test
    public void testPersonAddFunctionalDependencyDeterminant() {
        ImmutableSet<String> otherFD = viewDefinitions.stream()
                .map(RelationDefinition::getOtherFunctionalDependencies)
                .flatMap(Collection::stream)
                .map(FunctionalDependency::getDeterminants)
                .flatMap(Collection::stream)
                .map(d -> d.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("c_name"), otherFD);
    }

    /**
     * Non-null constraint taken into account
     */
    @Test
    public void testPersonAddNonNullConstraint() {
        ImmutableSet<String> nonNullColumns = viewDefinitions.stream()
                .map(RelationDefinition::getAttributes)
                .flatMap(Collection::stream)
                .filter(a -> !a.isNullable())
                .map(v -> v.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertTrue(nonNullColumns.contains("country"));
    }

    @Test
    public void testForeignKeySources() {
        ImmutableSet<String> nonNullColumns = viewDefinitions.stream()
                .map(RelationDefinition::getForeignKeys)
                .flatMap(Collection::stream)
                .map(ForeignKeyConstraint::getComponents)
                .flatMap(Collection::stream)
                .map(ForeignKeyConstraint.Component::getAttribute)
                .map(v -> v.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertTrue(nonNullColumns.contains("status"));
    }

    @Test
    public void testForeignKeyTargets() {
        ImmutableSet<String> nonNullColumns = viewDefinitions.stream()
                .map(RelationDefinition::getForeignKeys)
                .flatMap(Collection::stream)
                .map(ForeignKeyConstraint::getComponents)
                .flatMap(Collection::stream)
                .map(ForeignKeyConstraint.Component::getReferencedAttribute)
                .map(v -> v.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertTrue(nonNullColumns.contains("status_id"));
    }

}
