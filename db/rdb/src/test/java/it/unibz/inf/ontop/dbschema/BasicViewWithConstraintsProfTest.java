package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class BasicViewWithConstraintsProfTest {
    private static final String VIEW_FILE = "src/test/resources/prof/prof-basic-views-with-constraints.json";
    private static final String DBMETADATA_FILE = "src/test/resources/prof/prof_with_constraints.db-extract.json";

    ImmutableSet<OntopViewDefinition> viewDefinitions = loadViewDefinitions(VIEW_FILE, DBMETADATA_FILE);

    public BasicViewWithConstraintsProfTest() throws MetadataExtractionException, FileNotFoundException {
    }

    /**
     * Hidden columns disappear from constraints
     */
    @Test
    public void testProfUniqueConstraintOnHiddenColumns() throws Exception {
        Optional<OntopViewDefinition> firstView = viewDefinitions.stream().findFirst();
        List<String> constraints = firstView.get()
                .getUniqueConstraints()
                .stream()
                .map(c -> c.getAttributes())
                .flatMap(Collection::stream)
                .map(v -> v.getID().getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of("position", "a_id"), constraints);
    }

    /**
     * Switch PK from parent to added column. Parent PK becomes standard unique constraint.
     */
    @Test
    public void testProfPKChange() throws Exception {
        Optional<OntopViewDefinition> firstView = viewDefinitions.stream().findFirst();
        Map<Object, Object> constraints = firstView.get()
                .getUniqueConstraints()
                .stream()
                .collect(Collectors.toMap(c -> c.getName(), c -> c.isPrimaryKey()));

        Map<Object, Object> otherMap = new HashMap<Object, Object>();
        otherMap.put( "student_position_id", true);
        otherMap.put("academic_pkey",false);
        assertEquals(otherMap, constraints);
    }

    /**
     * The determinant of the FD is correctly added by a viewfile
     */
    @Test
    public void testProfFailAddingFunctionalDependencyOfHiddenColumn() throws Exception {
        Optional<OntopViewDefinition> firstView = viewDefinitions.stream().findFirst();
        List<String> otherFD = firstView.get()
                .getOtherFunctionalDependencies()
                .stream()
                .map(d -> d.getDeterminants())
                .flatMap(Collection::stream)
                .map(d -> d.getID().getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of(), otherFD);
    }

    protected ImmutableSet<OntopViewDefinition> loadViewDefinitions(String viewFilePath,
                                                                    String dbMetadataFilePath)
            throws MetadataExtractionException, FileNotFoundException {

        OntopSQLCoreConfiguration configuration = OntopSQLCoreConfiguration.defaultBuilder()
                .jdbcUrl("jdbc:h2:mem:nowhere")
                .jdbcDriver("org.h2.Driver")
                .build();

        Injector injector = configuration.getInjector();
        SerializedMetadataProvider.Factory serializedMetadataProviderFactory = injector.getInstance(SerializedMetadataProvider.Factory.class);
        OntopViewMetadataProvider.Factory viewMetadataProviderFactory = injector.getInstance(OntopViewMetadataProvider.Factory.class);

        SerializedMetadataProvider dbMetadataProvider = serializedMetadataProviderFactory.getMetadataProvider(
                new FileReader(dbMetadataFilePath));

        OntopViewMetadataProvider viewMetadataProvider = viewMetadataProviderFactory.getMetadataProvider(dbMetadataProvider,
                new FileReader(viewFilePath));

        ImmutableMetadata metadata = ImmutableMetadata.extractImmutableMetadata(viewMetadataProvider);

        return metadata.getAllRelations().stream()
                .filter(r -> r instanceof OntopViewDefinition)
                .map(r -> (OntopViewDefinition) r)
                .collect(ImmutableCollectors.toSet());
    }
}
