package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class BasicLensPropagateUCUpTest {
    private static final String LENS_FILE = "src/test/resources/propagate-uc-up/lenses.json";
    private static final String DBMETADATA_FILE = "src/test/resources/propagate-uc-up/metadata.db-extract.json";

    private final ImmutableSet<NamedRelationDefinition> relationDefinitions = LensParsingTest.loadLensesAndTablesH2(LENS_FILE, DBMETADATA_FILE);
    private final ImmutableMap<String, Lens> lensMap;
    private final ImmutableMap<String, NamedRelationDefinition> tableMap;

    public BasicLensPropagateUCUpTest() throws Exception {
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
    public void testLeafUC() {
        assertHasUCs(lensMap.get("lenses.l4"), ImmutableSet.of(ImmutableSet.of("id")));
    }

    @Test
    public void testDirectPropagation() {
        assertHasUCs(lensMap.get("lenses.l3"), ImmutableSet.of(ImmutableSet.of("id")));
    }

    @Test
    public void testTwoLevelPropagation() {
        assertHasUCs(lensMap.get("lenses.l2"), ImmutableSet.of(ImmutableSet.of("id")));
    }

    @Test
    public void testMultilevelPropagation() {
        assertHasUCs(lensMap.get("lenses.l1"), ImmutableSet.of(ImmutableSet.of("id")));
    }

    @Test
    public void testSourceTablePropagation() {
        assertHasUCs(tableMap.get("base_table"), ImmutableSet.of(ImmutableSet.of("id")));
    }

    //The 'x' lenses are loaded in opposite order (starting with the one that define the UC first. Thi way, recursive propagation should happen more often.
    @Test
    public void testOppositeOrderPropagation() {
        assertHasUCs(lensMap.get("lenses.x1"), ImmutableSet.of(ImmutableSet.of("id")));
    }

    @Test
    public void testNoPropagationFilter() {
        assertHasUCs(tableMap.get("base_table_2"), ImmutableSet.of());
    }

    @Test
    public void testNoPropagationOverride() {
        assertHasUCs(tableMap.get("base_table_3"), ImmutableSet.of());
    }

    @Test
    public void testNoPropagationCompositeOverride() {
        assertHasUCs(tableMap.get("base_table_4"), ImmutableSet.of());
    }

    @Test
    public void testPropagateCompositeConstraint() {
        assertHasUCs(tableMap.get("base_table_5"), ImmutableSet.of(ImmutableSet.of("a", "b")));
    }

    @Test
    public void testSomeVariablesOverridden() {
        assertHasUCs(tableMap.get("base_table_6"), ImmutableSet.of(ImmutableSet.of("c")));
    }

    @Test
    public void testPropagateMultipleConstraints() {
        assertHasUCs(tableMap.get("base_table_7"), ImmutableSet.of(ImmutableSet.of("a"), ImmutableSet.of("b")));
    }

    private void assertHasUCs(NamedRelationDefinition relation, ImmutableSet<ImmutableSet<String>> expected) {
        assertEquals(expected, relation.getUniqueConstraints().stream()
                .map(uc -> uc.getDeterminants().stream()
                        .map(det -> det.getID().getName())
                        .collect(Collectors.toSet()))
                .collect(Collectors.toSet()));
        assertEquals(expected.size(), relation.getUniqueConstraints().size());

    }


}
