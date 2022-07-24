package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.io.FileReader;
import java.io.Reader;

public class ViewDefinitionParsingTest {

    @Test
    public void testValidPersonBasicViews() throws Exception {

        ImmutableSet<OntopViewDefinition> viewDefinitions = loadViewDefinitions("src/test/resources/person/basic_views.json",
                "src/test/resources/person/person.db-extract.json");
    }

    @Test
    public void testValidProfBasicViews() throws Exception {
        ImmutableSet<OntopViewDefinition> viewDefinitions = loadViewDefinitions("src/test/resources/prof/prof-basic-views.json",
                "src/test/resources/prof/prof.db-extract.json");
    }

     /**
     * Hidden attribute present in newly added FD
     */
    @Test(expected = MetadataExtractionException.class)
    public void testValidProfBasicViews_MissingFDAttributes() throws Exception {
        ImmutableSet<OntopViewDefinition> viewDefinitions = loadViewDefinitions("src/test/resources/prof/prof-basic-views-with-constraints-hiddenFD.json",
                "src/test/resources/prof/prof_with_constraints.db-extract.json");
    }

    /**
     * Hidden attribute present in newly added UC
     */
    @Test(expected = MetadataExtractionException.class)
    public void testValidProfBasicViews_MissingUCAttributes() throws Exception {
        ImmutableSet<OntopViewDefinition> viewDefinitions = loadViewDefinitions("src/test/resources/prof/prof-basic-views-with-constraints-hiddenUC.json",
                "src/test/resources/prof/prof_with_constraints.db-extract.json");
    }

    public static ImmutableSet<OntopViewDefinition> loadViewDefinitions(String viewFilePath,
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
