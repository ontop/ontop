package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasicViewWithConstraintsParsingTest {
    private static final String VIEW_FILE = "src/test/resources/person/basic_views_with_constraints.json";
    private static final String DBMETADATA_FILE = "src/test/resources/person/person_with_constraints.db-extract.json";

    ImmutableSet<OntopViewDefinition> viewDefinitions = ViewDefinitionParsingTest.loadViewDefinitions(VIEW_FILE, DBMETADATA_FILE);

    public BasicViewWithConstraintsParsingTest() throws Exception {
    }

    /**
     * Both the parent "id" and added "status" constraints are present in the views
     */
    @Test
    public void testPersonAddUniqueConstraint() throws Exception {
        ImmutableSet<String> constraints = viewDefinitions.stream()
                .map(RelationDefinition::getUniqueConstraints)
                .flatMap(Collection::stream)
                .map(UniqueConstraint::getAttributes)
                .flatMap(Collection::stream)
                .map(v -> v.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("status", "id"), constraints);
    }

    /**
     * The dependent of the FD is correctly added by a viewfile
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

        assertEquals(ImmutableSet.of("country"), otherFD);
    }

    /**
     * The determinant of the FD is correctly added by a viewfile
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

    /**
     * Add FK destination relation name via viewfile
     */
    @Test
    public void testPersonAddForeignKey_DestinationRelation() throws Exception {
        ImmutableSet<String> destination_relation = viewDefinitions.stream()
                .map(RelationDefinition::getForeignKeys)
                .flatMap(Collection::stream)
                .map(ForeignKeyConstraint::getReferencedRelation)
                .map(d -> d.getID().getComponents().get(0).getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("statuses"), destination_relation);
    }

    /**
     * Add destination relation foreign key column name via viewfile
     */
    @Test
    public void testPersonAddForeignKey_DestinationColumn() throws Exception {
        ImmutableSet<String> destination_column = viewDefinitions.stream()
                .map(RelationDefinition::getForeignKeys)
                .flatMap(Collection::stream)
                .map(ForeignKeyConstraint::getComponents)
                .map(c -> c.get(0).getReferencedAttribute().getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("status_id"), destination_column);
    }

    /**
     * Add source relation key column name via viewfile
     */
    @Test
    public void testPersonAddForeignKey_SourceColumn() throws Exception {
        ImmutableSet<String> source_column = viewDefinitions.stream()
                .map(RelationDefinition::getForeignKeys)
                .flatMap(Collection::stream)
                .map(ForeignKeyConstraint::getComponents)
                .map(c -> c.get(0).getAttribute().getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("status"), source_column);
    }

    /**
     * Add new foreign key name
     */
    @Test
    public void testPersonAddForeignKey_FKName() throws Exception {
        ImmutableSet<String> fk_name = viewDefinitions.stream()
                .map(RelationDefinition::getForeignKeys)
                .flatMap(Collection::stream)
                .map(ForeignKeyConstraint::getName)
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("status_id_fkey", "status_desc_fkey"), fk_name);
    }

    /**
     * Composite foreign key
     */
    @Test
    public void testCompositeForeignKey() throws Exception {
        ImmutableList<String> destination_column = viewDefinitions.stream()
                .map(RelationDefinition::getForeignKeys)
                .flatMap(Collection::stream)
                .filter(fk -> fk.getName().equals("status_desc_fkey"))
                .map(ForeignKeyConstraint::getComponents)
                .flatMap(Collection::stream)
                .map(c -> c.getAttribute().getID().getName())
                .collect(ImmutableCollectors.toList());

        assertEquals(ImmutableList.of("status", "statusDescription"), destination_column);
    }


    /**
     * Non-null constraint taken into account
     */
    @Test
    public void testPersonAddNonNullConstraint() throws Exception {
        ImmutableSet<String> nonNullColumns = viewDefinitions.stream()
                .map(RelationDefinition::getAttributes)
                .flatMap(Collection::stream)
                .filter(a -> !a.isNullable())
                .map(v -> v.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertTrue(nonNullColumns.contains("country"));
    }
}
