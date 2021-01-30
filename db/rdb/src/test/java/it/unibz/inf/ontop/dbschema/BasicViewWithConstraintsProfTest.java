package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class BasicViewWithConstraintsProfTest {
    private static final String VIEW_FILE = "src/test/resources/prof/prof-basic-views-with-constraints.json";
    private static final String DBMETADATA_FILE = "src/test/resources/prof/prof_with_constraints.db-extract.json";

    ImmutableSet<OntopViewDefinition> viewDefinitions = loadViewDefinitions(VIEW_FILE, DBMETADATA_FILE);

    public BasicViewWithConstraintsProfTest() throws Exception {
    }

    /**
     * Hidden columns disappear from constraints
     */
    @Test
    public void testProfUniqueConstraintOnHiddenColumns() throws Exception {
        List<String> constraints = viewDefinitions.stream()
                .map(v -> v.getUniqueConstraints())
                .flatMap(Collection::stream)
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
        Map<String, Boolean> constraints = viewDefinitions.stream()
                .map(v -> v.getUniqueConstraints())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(UniqueConstraint::getName, UniqueConstraint::isPrimaryKey));

        Map<String, Boolean> otherMap = ImmutableMap.of("student_position_id", true,
                                                        "academic_pkey",false);
        assertEquals(otherMap, constraints);
    }

    /**
     * The FD is not added when the determinant is missing as a column
     */
    @Test
    public void testProfFailAddingFunctionalDependencyOfHiddenColumn() throws Exception {
        List<String> otherFD = viewDefinitions.stream()
                .map(v -> v.getOtherFunctionalDependencies())
                .flatMap(Collection::stream)
                .map(d -> d.getDeterminants())
                .flatMap(Collection::stream)
                .map(d -> d.getID().getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of(), otherFD);
    }

    protected ImmutableSet<OntopViewDefinition> loadViewDefinitions(String viewFilePath,
                                                                    String dbMetadataFilePath)
            throws Exception {

        OntopSQLCoreConfiguration configuration = OntopSQLCoreConfiguration.defaultBuilder()
                .jdbcUrl("jdbc:h2:mem:nowhere")
                .jdbcDriver("org.h2.Driver")
                .build();

        Injector injector = configuration.getInjector();
        SerializedMetadataProvider.Factory serializedMetadataProviderFactory = injector.getInstance(SerializedMetadataProvider.Factory.class);
        OntopViewMetadataProvider.Factory viewMetadataProviderFactory = injector.getInstance(OntopViewMetadataProvider.Factory.class);

        SerializedMetadataProvider dbMetadataProvider;
        try (Reader dbMetadataReader = new FileReader(dbMetadataFilePath)) {
            dbMetadataProvider = serializedMetadataProviderFactory.getMetadataProvider(dbMetadataReader);
        }

        OntopViewMetadataProvider viewMetadataProvider;
        try (Reader viewReader = new FileReader(viewFilePath)) {
            viewMetadataProvider = viewMetadataProviderFactory.getMetadataProvider(dbMetadataProvider, viewReader);
        }

        ImmutableMetadata metadata = ImmutableMetadata.extractImmutableMetadata(viewMetadataProvider);

        return metadata.getAllRelations().stream()
                .filter(r -> r instanceof OntopViewDefinition)
                .map(r -> (OntopViewDefinition) r)
                .collect(ImmutableCollectors.toSet());
    }
}
