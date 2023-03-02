package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class UnionLensTest {
    private static final String LENS_FILE = "src/test/resources/union/union_lenses.json";
    private static final String DBMETADATA_FILE = "src/test/resources/union/union.db-extract.json";

    private final ImmutableSet<Lens> lensDefinitions = LensParsingTest.loadLensesH2(LENS_FILE, DBMETADATA_FILE);
    private final ImmutableMap<String, Lens> lensMap;

    public UnionLensTest() throws Exception {
        lensMap = lensDefinitions.stream().collect(ImmutableCollectors.<Lens, String, Lens>toMap(
                l -> String.join(".", l.getID().getComponents().reverse().stream().map(c -> c.getName()).collect(Collectors.toList())),
                l -> l
        ));
    }

    @Test
    public void testInheritedUniqueConstraint() {
        var constraints = lensMap.get("lenses.l1").getUniqueConstraints();
        assertEquals(1, constraints.size());
        assertEquals(ImmutableSet.of("id", "prov"), constraints.get(0).getAttributes().stream().map(a -> a.getID().getName()).collect(ImmutableCollectors.toSet()));
    }

    @Test
    public void testDifferentOrder() {
        var constraints = lensMap.get("lenses.differentOrder").getUniqueConstraints();
        assertEquals(1, constraints.size());
        assertEquals(ImmutableSet.of("id", "prov"), constraints.get(0).getAttributes().stream().map(a -> a.getID().getName()).collect(ImmutableCollectors.toSet()));
    }

    @Test
    public void testInheritedNoUniqueConstraint() {
        var constraints = lensMap.get("lenses.specialized").getUniqueConstraints();
        assertEquals(0, constraints.size());
    }

    @Test
    public void testUniqueConstraintsFromMakeDistinct() {
        var constraints = lensMap.get("lenses.extras").getUniqueConstraints();
        assertEquals(1, constraints.size());
        assertEquals(ImmutableSet.of("id", "info", "provenance"), constraints.get(0).getAttributes().stream().map(a -> a.getID().getName()).collect(ImmutableCollectors.toSet()));
    }

    @Test
    public void testUniqueNotInferredIfNoProvenanceColumn() {
        var lens = lensMap.get("lenses.l2");
        var constraints = lens.getUniqueConstraints();
        assertEquals(0, constraints.size());
        assertEquals(2, lens.getAttributes().size());
    }

    @Test
    public void testUniqueNotInferredIfOneChildNotUnique() {
        var lens = lensMap.get("lenses.l3");
        var constraints = lens.getUniqueConstraints();
        assertEquals(0, constraints.size());
        assertEquals(3, lens.getAttributes().size());
    }

    @Test
    public void testProjectedChildrenKeepUnique() {
        var lens = lensMap.get("lenses.specializedWithX");
        var constraints = lens.getUniqueConstraints();
        assertEquals(1, constraints.size());
        assertEquals(2, constraints.get(0).getDeterminants().size());
    }

    @Test
    public void testTripleUnionKeepsUnique() {
        var lens = lensMap.get("lenses.tripleUnion");
        var constraints = lens.getUniqueConstraints();
        assertEquals(1, constraints.size());
        assertEquals(2, constraints.get(0).getDeterminants().size());
    }

    @Test
    public void testUnionOfSameTableLosesUnique() {
        var lens = lensMap.get("lenses.cloneUnion");
        var constraints = lens.getUniqueConstraints();
        assertEquals(0, constraints.size());
    }

    @Test
    public void testNullableIsInherited() {
        var lens = lensMap.get("lenses.nullable");
        var id = lens.getAttribute(1);
        assertTrue(id.isNullable());
    }

    @Test
    public void testNullableKeepsUniqueConstraints() {
        var lens = lensMap.get("lenses.nullable");
        var constraints = lens.getUniqueConstraints();
        assertEquals(1, constraints.size());
    }

    @Test
    public void testAddedConstraints() {
        var lens = lensMap.get("lenses.added");
        var constraints = lens.getUniqueConstraints();
        assertEquals(2, constraints.size());
        assertEquals(1, lens.getOtherFunctionalDependencies().size());
        assertEquals(1, lens.getForeignKeys().size());
        assertFalse(lens.getAttribute(1).isNullable());
    }

    @Test
    public void testBadUnionDifferentColumnNames() {
        assertLoadFails("src/test/resources/union/bad_lenses/different_names.json");
    }

    @Test
    public void testBadUnionDifferentColumnTypes() {
        assertLoadFails("src/test/resources/union/bad_lenses/different_types.json");
    }

    @Test
    public void testBadUnionDifferentColumnCount() {
        assertLoadFails("src/test/resources/union/bad_lenses/different_column_count.json");
    }

    private void assertLoadFails(String path) {
        try {
            var lensDefinitions = LensParsingTest.loadLensesH2(path, DBMETADATA_FILE);
            assert(false); //code should never reach this line
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //correct
        }
    }



}
