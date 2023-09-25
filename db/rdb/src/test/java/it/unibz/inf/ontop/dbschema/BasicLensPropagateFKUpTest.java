package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class BasicLensPropagateFKUpTest {
    private static final String LENS_FILE = "src/test/resources/propagate-fk-up/lenses.json";
    private static final String DBMETADATA_FILE = "src/test/resources/propagate-fk-up/metadata.db-extract.json";

    private final ImmutableSet<NamedRelationDefinition> relationDefinitions = LensParsingTest.loadLensesAndTablesH2(LENS_FILE, DBMETADATA_FILE);
    private final ImmutableMap<String, Lens> lensMap;
    private final ImmutableMap<String, NamedRelationDefinition> tableMap;

    public BasicLensPropagateFKUpTest() throws Exception {
        lensMap = relationDefinitions.stream()
                .filter(l -> l instanceof Lens)
                .map(l -> (Lens)l)
                .collect(ImmutableCollectors.<Lens, String, Lens>toMap(
                    l -> String.join(".", l.getID().getComponents().reverse().stream()
                            .map(c -> c.getName())
                            .collect(Collectors.toList())),
                    l -> l
        ));
        tableMap = relationDefinitions.stream()
                .filter(t -> !(t instanceof Lens))
                .collect(ImmutableCollectors.<NamedRelationDefinition, String, NamedRelationDefinition>toMap(
                        t -> String.join(".", t.getID().getComponents().reverse()
                                .stream().map(c -> c.getName())
                                .collect(Collectors.toList())),
                        t -> t
                ));
    }

    @Test
    public void testLeafFK() {
        assertHasFKs(lensMap.get("lenses.l4"), ImmutableSet.of(
                new ForeignKey("\"A\"", "id_A", "id"),
                new ForeignKey("\"B\"", "id_B_1", "id_1", "id_B_2", "id_2")
        ));
    }

    @Test
    public void testDirectPropagation() {
        assertHasFKs(lensMap.get("lenses.l3"), ImmutableSet.of(
                new ForeignKey("\"A\"", "id_A", "id"),
                new ForeignKey("\"B\"", "id_B_1", "id_1", "id_B_2", "id_2")
        ));
    }

    @Test
    public void testTwoLevelPropagation() {
        assertHasFKs(lensMap.get("lenses.l2"), ImmutableSet.of(
                new ForeignKey("\"A\"", "id_A", "id"),
                new ForeignKey("\"B\"", "id_B_1", "id_1", "id_B_2", "id_2")
        ));
    }

    @Test
    public void testMultilevelPropagation() {
        assertHasFKs(lensMap.get("lenses.l1"), ImmutableSet.of(
                new ForeignKey("\"A\"", "id_A", "id"),
                new ForeignKey("\"B\"", "id_B_1", "id_1", "id_B_2", "id_2")
        ));
    }

    @Test
    public void testSourceTablePropagation() {
        assertHasFKs(tableMap.get("base_table"), ImmutableSet.of(
                new ForeignKey("\"A\"", "id_A", "id"),
                new ForeignKey("\"B\"", "id_B_1", "id_1", "id_B_2", "id_2")
        ));
    }

    //The 'x' lenses are loaded in opposite order (starting with the one that defines the FK first. This way, recursive propagation should happen more often.
    @Test
    public void testOppositeOrderPropagation() {
        assertHasFKs(lensMap.get("lenses.x1"), ImmutableSet.of(
                new ForeignKey("\"A\"", "id_A", "id"),
                new ForeignKey("\"B\"", "id_B_1", "id_1", "id_B_2", "id_2")
        ));
    }

    @Test
    public void testNoPropagationIsParent() {
        assertHasFKs(tableMap.get("A"), ImmutableSet.of());
    }

    @Test
    public void testNoPropagationOverride() {
        assertHasFKs(tableMap.get("base_table_3"), ImmutableSet.of());
    }

    @Test
    public void testSomeVariablesOverridden() {
        assertHasFKs(tableMap.get("base_table_4"), ImmutableSet.of(
                new ForeignKey("\"A\"", "id_A", "id"),
                new ForeignKey("\"B\"", "id_B_1", "id_1", "id_B_2", "id_2")
        ));
    }

    @Test
    public void testUCsStillInferred() {
        assertHasUCs(tableMap.get("base_table"), ImmutableSet.of(ImmutableSet.of("id_A", "id_B_1", "id_B_2")));
    }

    private void assertHasUCs(NamedRelationDefinition relation, ImmutableSet<ImmutableSet<String>> expected) {
        assertEquals(expected, relation.getUniqueConstraints().stream()
                .map(uc -> uc.getDeterminants().stream()
                        .map(det -> det.getID().getName())
                        .collect(Collectors.toSet()))
                .collect(Collectors.toSet()));
        assertEquals(expected.size(), relation.getUniqueConstraints().size());
    }

    private void assertHasFKs(NamedRelationDefinition relation, ImmutableSet<ForeignKey> expected) {
        var actual = relation.getForeignKeys().stream()
                .map(ForeignKey::new)
                .collect(ImmutableCollectors.toSet());
        assertEquals(expected, actual);
    }

    private static class ForeignKey {
        private final String targetRelation;
        private final ImmutableMap<String, String> mappedColumns;

        protected ForeignKey(ForeignKeyConstraint fk) {
            this.targetRelation = fk.getReferencedRelation().getID().getSQLRendering();
            this.mappedColumns = fk.getComponents().stream()
                    .collect(ImmutableCollectors.toMap(
                            c -> c.getAttribute().getID().getName(),
                            c -> c.getReferencedAttribute().getID().getName()
                    ));
        }

        protected ForeignKey(String targetRelation, String... mapping) {
            this.targetRelation = targetRelation;
            this.mappedColumns = IntStream.range(0, mapping.length / 2)
                    .mapToObj(i -> Maps.immutableEntry(mapping[i * 2], mapping[i * 2 + 1]))
                    .collect(ImmutableCollectors.toMap());
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof ForeignKey))
                return false;
            var fk = (ForeignKey) obj;
            return fk.targetRelation.equals(targetRelation) && fk.mappedColumns.equals(mappedColumns);
        }

        @Override
        public String toString() {
            var keys = ImmutableList.copyOf(mappedColumns.keySet());
            return String.format("FK --> %s:\n[%s] --> [%s]",
                    targetRelation,
                    String.join(", ", keys),
                    keys.stream()
                            .map(mappedColumns::get)
                            .collect(Collectors.joining(", ")));
        }

        @Override
        public int hashCode() {
            return ImmutableSet.of(targetRelation.hashCode(), mappedColumns.hashCode()).hashCode();
        }
    }


}
