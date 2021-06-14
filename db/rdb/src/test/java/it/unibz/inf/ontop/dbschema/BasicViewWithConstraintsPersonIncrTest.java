package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class BasicViewWithConstraintsPersonIncrTest {
    private static final String VIEW_FILE = "src/test/resources/person/basic_views_with_constraints.json";
    private static final String DBMETADATA_FILE = "src/test/resources/person/person_with_FD.db-extract.json";

    ImmutableSet<OntopViewDefinition> viewDefinitions = ViewDefinitionParsingTest.loadViewDefinitions(VIEW_FILE, DBMETADATA_FILE);

    public BasicViewWithConstraintsPersonIncrTest() throws Exception {
    }

    /**
     * The dependents of the FDs with identical determinants are merged
     */
    @Test
    public void testPersonAddFunctionalDependencyDependent() throws Exception {
        ImmutableSet<String> otherFD = viewDefinitions.stream()
                .map(RelationDefinition::getOtherFunctionalDependencies)
                .flatMap(Collection::stream)
                .map(FunctionalDependency::getDependents)
                .flatMap(Collection::stream)
                .map(d -> d.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("status", "country"), otherFD);
    }

    /**
     * The determinant of the FD is correctly added by a viewfile and used to merge FDs
     */
    @Test
    public void testPersonAddFunctionalDependencyDeterminant() throws Exception {
        ImmutableSet<String> otherFD = viewDefinitions.stream()
                .map(RelationDefinition::getOtherFunctionalDependencies)
                .flatMap(Collection::stream)
                .map(FunctionalDependency::getDeterminants)
                .flatMap(Collection::stream)
                .map(d -> d.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("locality"), otherFD);
    }
}
