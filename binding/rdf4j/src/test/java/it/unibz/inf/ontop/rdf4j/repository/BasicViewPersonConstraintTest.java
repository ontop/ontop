package it.unibz.inf.ontop.rdf4j.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class BasicViewPersonConstraintTest extends AbstractRDF4JTest {
    private static final String VIEW_FILE = "src/test/resources/person/views/basic_views_with_constraints.json";
    private static final String DBMETADATA_FILE = "src/test/resources/person/person_with_constraints.db-extract.json";

    ImmutableSet<OntopViewDefinition> viewDefinitions = loadViewDefinitions(VIEW_FILE, DBMETADATA_FILE);

    public BasicViewPersonConstraintTest() throws MetadataExtractionException, FileNotFoundException {
    }

    /**
     * Both the parent "id" and added "status" constraints are present in the views
     */
    @Test
    public void testPersonAddUniqueConstraint() throws Exception {
        Optional<OntopViewDefinition> firstView = viewDefinitions.stream().findFirst();
        List<String> constraints = firstView.get().getIQ().getTree().getChildren()
                .stream()
                .map(i -> i.inferUniqueConstraints())
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .map(v -> v.getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of("status", "id"), constraints);
    }

    /**
     * The dependent of the FD is correctly added by a viewfile
     */
    @Test // Add "locality" as being functionally dependent on "country"
    public void testPersonAddFunctionalDependencyDependent() throws Exception {
        Optional<OntopViewDefinition> firstView = viewDefinitions.stream().findFirst();
        List<ExtensionalDataNode> extensionalDataNodeList = (List<ExtensionalDataNode>)(List<?>) firstView.get().getIQ().getTree().getChildren();

        List<String> otherFD = extensionalDataNodeList.stream()
                .map(e -> e.getRelationDefinition())
                .map(r -> r.getOtherFunctionalDependencies())
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
        Optional<OntopViewDefinition> firstView = viewDefinitions.stream().findFirst();
        List<ExtensionalDataNode> extensionalDataNodeList = (List<ExtensionalDataNode>)(List<?>) firstView.get().getIQ().getTree().getChildren();

        List<String> otherFD = extensionalDataNodeList.stream()
                .map(e -> e.getRelationDefinition())
                .map(r -> r.getOtherFunctionalDependencies())
                .flatMap(Collection::stream)
                .map(d -> d.getDeterminants())
                .flatMap(Collection::stream)
                .map(d -> d.getID().getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of("locality"), otherFD);
    }

    /**
     * Add destination relation name via viewfile
     */
    @Test
    public void testPersonAddForeignKey_DestinationRelation() throws Exception {
        Optional<OntopViewDefinition> firstView = viewDefinitions.stream().findFirst();
        List<ExtensionalDataNode> extensionalDataNodeList = (List<ExtensionalDataNode>)(List<?>) firstView.get().getIQ().getTree().getChildren();

        List<String> destination_relation = extensionalDataNodeList.stream()
                .map(e -> e.getRelationDefinition())
                .map(r -> r.getForeignKeys())
                .flatMap(Collection::stream)
                .map(d -> d.getReferencedRelation())
                //.flatMap(Collection::stream)
                .map(d -> d.getID().getComponents().get(0).getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of("statuses"), destination_relation);
    }

    /**
     * Add destination relation foreign key column name via viewfile
     */
    @Test
    public void testPersonAddForeignKey_DestinationColumn() throws Exception {
        Optional<OntopViewDefinition> firstView = viewDefinitions.stream().findFirst();
        List<ExtensionalDataNode> extensionalDataNodeList = (List<ExtensionalDataNode>)(List<?>) firstView.get().getIQ().getTree().getChildren();

        List<String> destination_column = extensionalDataNodeList.stream()
                .map(e -> e.getRelationDefinition())
                .map(r -> r.getForeignKeys())
                .flatMap(Collection::stream)
                .map(d -> d.getComponents())
                //.flatMap(Collection::stream)
                .map(d -> d.get(0).getReferencedAttribute().getID().getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of("status_id"), destination_column);
    }

    /**
     * Add source relation key column name via viewfile
     */
    @Test
    public void testPersonAddForeignKey_SourceColumn() throws Exception {
        Optional<OntopViewDefinition> firstView = viewDefinitions.stream().findFirst();
        List<ExtensionalDataNode> extensionalDataNodeList = (List<ExtensionalDataNode>)(List<?>) firstView.get().getIQ().getTree().getChildren();

        List<String> source_column = extensionalDataNodeList.stream()
                .map(e -> e.getRelationDefinition())
                .map(r -> r.getForeignKeys())
                .flatMap(Collection::stream)
                .map(d -> d.getComponents())
                //.flatMap(Collection::stream)
                .map(d -> d.get(0).getAttribute().getID().getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of("status"), source_column);
    }

    /**
     * Add new foreign key name
     */
    @Test
    public void testPersonAddForeignKey_FKName() throws Exception {
        Optional<OntopViewDefinition> firstView = viewDefinitions.stream().findFirst();
        List<ExtensionalDataNode> extensionalDataNodeList = (List<ExtensionalDataNode>)(List<?>) firstView.get().getIQ().getTree().getChildren();

        List<String> source_column = extensionalDataNodeList.stream()
                .map(e -> e.getRelationDefinition())
                .map(r -> r.getForeignKeys())
                .flatMap(Collection::stream)
                .map(d -> d.getName())
                .collect(Collectors.toList());

        assertEquals(ImmutableList.of("status_id_fkey"), source_column);
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
