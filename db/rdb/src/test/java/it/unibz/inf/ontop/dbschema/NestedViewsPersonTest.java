package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class NestedViewsPersonTest {

    private static final String VIEW_FILE = "src/test/resources/person/nested_lenses.json";
    private static final String DBMETADATA_FILE = "src/test/resources/person/hr_person-xt.db-extract.json";

    ImmutableSet<Lens> viewDefinitions = LensParsingTest.loadViewDefinitionsPostgres(VIEW_FILE, DBMETADATA_FILE);

    public NestedViewsPersonTest() throws Exception {
    }

    @Test
    public void testNoUC() {
        ImmutableSet<String> constraints = viewDefinitions.stream()
                .map(RelationDefinition::getUniqueConstraints)
                .flatMap(Collection::stream)
                .map(UniqueConstraint::getAttributes)
                .flatMap(Collection::stream)
                .map(v -> v.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(constraints, ImmutableSet.of());
    }

    @Test
    public void testInferredFD() {
        ImmutableSet<FunctionalDependency> fds = viewDefinitions.stream()
                .map(RelationDefinition::getOtherFunctionalDependencies)
                .flatMap(Collection::stream)
                .collect(ImmutableCollectors.toSet());
        assertEquals(1, fds.size());

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
}
