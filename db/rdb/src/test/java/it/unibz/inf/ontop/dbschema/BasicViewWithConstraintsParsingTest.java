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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class BasicViewWithConstraintsParsingTest {
    private static final String VIEW_FILE = "src/test/resources/person/basic_views_with_constraints.json";
    private static final String DBMETADATA_FILE = "src/test/resources/person/person_with_constraints.db-extract.json";

    ImmutableSet<OntopViewDefinition> viewDefinitions = loadViewDefinitions(VIEW_FILE, DBMETADATA_FILE);

    public BasicViewWithConstraintsParsingTest() throws MetadataExtractionException, FileNotFoundException {
    }

    /**
     * Both the parent "id" and added "status" constraints are present in the views
     */
    @Test
    public void testPersonAddUniqueConstraint() throws Exception {
        List<String> constraints = viewDefinitions.stream()
                .map(v -> v.getUniqueConstraints())
                .flatMap(Collection::stream)
                .map(c -> c.getAttributes())
                .flatMap(Collection::stream)
                .map(v -> v.getID().getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of("status", "id"), constraints);
    }

    /**
     * The dependent of the FD is correctly added by a viewfile
     */
    @Test
    public void testPersonAddFunctionalDependencyDependent() throws Exception {
        List<String> otherFD = viewDefinitions.stream()
                .map(v -> v.getOtherFunctionalDependencies())
                .flatMap(Collection::stream)
                .map(d -> d.getDependents())
                .flatMap(Collection::stream)
                .map(d -> d.getID().getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of("country"), otherFD);
    }

    /**
     * The determinant of the FD is correctly added by a viewfile
     */
    @Test
    public void testPersonAddFunctionalDependencyDeterminant() throws Exception {
        List<String> otherFD = viewDefinitions.stream()
                .map(v -> v.getOtherFunctionalDependencies())
                .flatMap(Collection::stream)
                .map(d -> d.getDeterminants())
                .flatMap(Collection::stream)
                .map(d -> d.getID().getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of("locality"), otherFD);
    }

    /**
     * Add FK destination relation name via viewfile
     */
    @Test
    public void testPersonAddForeignKey_DestinationRelation() throws Exception {
        List<String> destination_relation = viewDefinitions.stream()
                .map(v -> v.getForeignKeys())
                .flatMap(Collection::stream)
                .map(f -> f.getReferencedRelation())
                .map(d -> d.getID().getComponents().get(0).getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of("statuses"), destination_relation);
    }

    /**
     * Add destination relation foreign key column name via viewfile
     */
    @Test
    public void testPersonAddForeignKey_DestinationColumn() throws Exception {
        List<String> destination_column = viewDefinitions.stream()
                .map(v -> v.getForeignKeys())
                .flatMap(Collection::stream)
                .map(f -> f.getComponents())
                .map(c -> c.get(0).getReferencedAttribute().getID().getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of("status_id"), destination_column);
    }

    /**
     * Add source relation key column name via viewfile
     */
    @Test
    public void testPersonAddForeignKey_SourceColumn() throws Exception {
        List<String> source_column = viewDefinitions.stream()
                .map(v -> v.getForeignKeys())
                .flatMap(Collection::stream)
                .map(f -> f.getComponents())
                .map(c -> c.get(0).getAttribute().getID().getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of("status"), source_column);
    }

    /**
     * Add new foreign key name
     */
    @Test
    public void testPersonAddForeignKey_FKName() throws Exception {
        List<String> fk_name = viewDefinitions.stream()
                .map(v -> v.getForeignKeys())
                .flatMap(Collection::stream)
                .map(f -> f.getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of("status_id_fkey"), fk_name);
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
