package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LostUniqueConstraintTest {
    private static final String VIEW_FILE = "src/test/resources/lostUniqueConstraint/lost_lens.json";
    private static final String DBMETADATA_FILE = "src/test/resources/lostUniqueConstraint/lost.db-extract.json";

    private final ImmutableSet<Lens> viewDefinitions = LensParsingTest.loadLensesH2(VIEW_FILE, DBMETADATA_FILE);

    public LostUniqueConstraintTest() throws Exception {
    }

    @Test
    public void testPersonUniqueConstraint() {
        ImmutableSet<String> constraints = viewDefinitions.stream()
                .map(RelationDefinition::getUniqueConstraints)
                .flatMap(Collection::stream)
                .map(UniqueConstraint::getAttributes)
                .flatMap(Collection::stream)
                .map(v -> v.getID().getName())
                .collect(ImmutableCollectors.toSet());

        assertEquals(ImmutableSet.of("id", "id2"), constraints);
    }

}
