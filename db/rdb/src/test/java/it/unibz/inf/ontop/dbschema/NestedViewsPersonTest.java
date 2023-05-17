package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class NestedViewsPersonTest {

    private static final String VIEW_FILE = "src/test/resources/person/nested_lenses.json";
    private static final String DBMETADATA_FILE = "src/test/resources/person/hr_person-xt.db-extract.json";

    private final ImmutableSet<Lens> viewDefinitions = LensParsingTest.loadViewDefinitionsPostgres(VIEW_FILE, DBMETADATA_FILE);

    public NestedViewsPersonTest() throws Exception {
    }

    @Test
    public void testInferredUC() {
        ImmutableSet<String> constraints = viewDefinitions.stream()
                .map(RelationDefinition::getUniqueConstraints)
                .flatMap(Collection::stream)
                .map(UniqueConstraint::getAttributes)
                .flatMap(Collection::stream)
                .map(v -> v.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(constraints, ImmutableSet.of("id", "pos"));
    }

    @Test
    public void testInferredFD() {
        ImmutableSet<FunctionalDependency> fds = viewDefinitions.stream()
                .map(RelationDefinition::getOtherFunctionalDependencies)
                .flatMap(Collection::stream)
                .collect(ImmutableCollectors.toSet());
        assertEquals(2, fds.size());

        FunctionalDependency fd = fds.stream().findFirst().get();
        ImmutableSet<String> determinants = fd.getDeterminants().stream()
                .map(v -> v.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(determinants, ImmutableSet.of("id"));

        ImmutableSet<String> dependents = fd.getDependents().stream()
                .map(v -> v.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("ssn", "fullName"), dependents);
    }

    @Test
    public void testInferredFKs() {
        ImmutableSet<ForeignKeyConstraint> fks = viewDefinitions.stream()
                .map(RelationDefinition::getForeignKeys)
                .flatMap(Collection::stream)
                .collect(ImmutableCollectors.toSet());
        assertEquals(4, fks.size());

        ForeignKeyConstraint fk = fks.stream().findFirst().get();
        String target = fk.getReferencedRelation().getID().getSQLRendering();
        assertEquals("\"hr\".\"person-xt\"", target);

        ImmutableSet<String> keys = fk.getComponents().stream()
                .map(ForeignKeyConstraint.Component::getAttribute)
                .map(a -> a.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("id"), keys);
    }
}
