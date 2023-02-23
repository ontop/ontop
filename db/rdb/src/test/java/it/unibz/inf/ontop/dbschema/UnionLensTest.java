package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UnionLensTest {
    private static final String VIEW_FILE = "src/test/resources/union/union_lenses.json";
    private static final String DBMETADATA_FILE = "src/test/resources/union/union.db-extract.json";

    private final ImmutableSet<Lens> viewDefinitions = LensParsingTest.loadLensesH2(VIEW_FILE, DBMETADATA_FILE);
    private final ImmutableMap<String, Lens> viewMap;

    public UnionLensTest() throws Exception {
        viewMap = viewDefinitions.stream().collect(ImmutableCollectors.<Lens, String, Lens>toMap(
                l -> String.join(".", l.getID().getComponents().stream().map(c -> c.getName()).collect(Collectors.toList())),
                l -> l
        ));
    }

    @Test
    public void testInheritedUniqueConstraint() {
        var constraints = viewMap.get("l1.lenses").getUniqueConstraints();
        assertEquals(constraints.size(), 1);
        assertEquals(ImmutableSet.of("id", "prov"), constraints.get(0).getAttributes().stream().map(a -> a.getID().getName()).collect(ImmutableCollectors.toSet()));
    }

    @Test
    public void testDifferentOrder() {
        var constraints = viewMap.get("differentOrder.lenses").getUniqueConstraints();
        assertEquals(constraints.size(), 1);
        assertEquals(ImmutableSet.of("id", "prov"), constraints.get(0).getAttributes().stream().map(a -> a.getID().getName()).collect(ImmutableCollectors.toSet()));
    }

    @Test
    public void testInheritedNoUniqueConstraint() {
        var constraints = viewMap.get("specialized.lenses").getUniqueConstraints();
        assertEquals(constraints.size(), 0);
    }

    @Test
    public void testUniqueConstraintsFromMakeDistinct() {
        var constraints = viewMap.get("extras.lenses").getUniqueConstraints();
        assertEquals(constraints.size(), 1);
        assertEquals(ImmutableSet.of("id", "info", "provenance"), constraints.get(0).getAttributes().stream().map(a -> a.getID().getName()).collect(ImmutableCollectors.toSet()));
    }

    @Test
    public void testUniqueNotInferredIfNoProvenanceColumn() {
        var lens = viewMap.get("l2.lenses");
        var constraints = lens.getUniqueConstraints();
        assertEquals(constraints.size(), 0);
        assertEquals(lens.getAttributes().size(), 2);
    }

    @Test
    public void testUniqueNotInferredIfOneChildNotUnique() {
        var lens = viewMap.get("l3.lenses");
        var constraints = lens.getUniqueConstraints();
        assertEquals(constraints.size(), 0);
        assertEquals(lens.getAttributes().size(), 3);
    }

    @Test
    public void testBadUnionDifferentColumnNames() {
        assertLoadFails("src/test/resources/union/bad_lenses/different_names.json");
    }

    @Test
    public void testBadUnionDifferentColumnTypes() {
        assertLoadFails("src/test/resources/union/bad_lenses/different_types.json");
    }

    private void assertLoadFails(String path) {
        try {
            var viewDefinitions = LensParsingTest.loadLensesH2(path, DBMETADATA_FILE);
            assert(false); //code should never reach this line
        } catch (Exception e) {
            //correct
        }
    }



}
