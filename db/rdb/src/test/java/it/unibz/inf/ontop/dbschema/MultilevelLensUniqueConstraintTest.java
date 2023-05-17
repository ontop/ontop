package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class MultilevelLensUniqueConstraintTest {
    private static final String LENS_FILE = "src/test/resources/multilevel-uc/multilevel-uc-lenses.json";
    private static final String DBMETADATA_FILE = "src/test/resources/multilevel-uc/multilevel-uc.db-extract.json";

    private final ImmutableSet<Lens> lensDefinitions = LensParsingTest.loadLensesH2(LENS_FILE, DBMETADATA_FILE);
    private final ImmutableMap<String, Lens> lensMap;

    public MultilevelLensUniqueConstraintTest() throws Exception {
        lensMap = lensDefinitions.stream().collect(ImmutableCollectors.<Lens, String, Lens>toMap(
                l -> String.join(".", l.getID().getComponents().reverse().stream().map(c -> c.getName()).collect(Collectors.toList())),
                l -> l
        ));
    }

    @Test
    public void testLevel1UC() {
        var constraints = lensMap.get("lenses.l1").getUniqueConstraints();
        assertEquals(1, constraints.size());
        assertEquals(ImmutableSet.of("id"), constraints.get(0).getAttributes().stream().map(a -> a.getID().getName()).collect(ImmutableCollectors.toSet()));
    }

    @Test
    public void testLevel2UC() {
        var constraints = lensMap.get("lenses.l2").getUniqueConstraints();
        assertEquals(1, constraints.size());
        assertEquals(ImmutableSet.of("id"), constraints.get(0).getAttributes().stream().map(a -> a.getID().getName()).collect(ImmutableCollectors.toSet()));
    }
}
