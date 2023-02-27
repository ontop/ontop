package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LostUniqueConstraintTest {
    private static final String LENS_FILE = "src/test/resources/lostUniqueConstraint/lost_lens.json";
    private static final String DBMETADATA_FILE = "src/test/resources/lostUniqueConstraint/lost.db-extract.json";

    private final ImmutableSet<Lens> lensDefinitions = LensParsingTest.loadLensesH2(LENS_FILE, DBMETADATA_FILE);

    public LostUniqueConstraintTest() throws Exception {
    }

    @Test
    public void testPersonUniqueConstraint() {
        var first = lensDefinitions.stream().findFirst().get();
        var second = lensDefinitions.stream().skip(1).findFirst().get();
        assertEquals(1, first.getUniqueConstraints().size());
        assertEquals(1, second.getUniqueConstraints().size());
    }

}
