package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.DuplicateFormatFlagsException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ViewDefinitionParsingTest {

    @Test
    public void testValidPersonBasicViews() throws MetadataExtractionException, FileNotFoundException {

        ImmutableSet<OntopViewDefinition> viewDefinitions = loadViewDefinitions("/person/basic_views.json", "/person/person.db-extract.json");

        // TODO: continue
    }

    @Test
    public void testValidProfBasicViews() throws MetadataExtractionException, FileNotFoundException {

        ImmutableSet<OntopViewDefinition> viewDefinitions = loadViewDefinitions("/prof/prof-basic-views.json", "/prof/prof.db-extract.json");
    }

    /**
     * Duplicate unique constraints defined in view
     */
    @Test(expected = IllegalArgumentException.class)
    public void testValidProfBasicViews_DuplicateUniqueConstraints() throws MetadataExtractionException, FileNotFoundException {
        ImmutableSet<OntopViewDefinition> viewDefinitions = loadViewDefinitions("/prof/prof-basic-views-with-constraints-duplicate-constraints.json", "/prof/prof_with_constraints.db-extract.json");
    }

    /**
     * Duplicate functional dependency defined in view - TEST FAILS
     * TODO: User can duplicate functional dependencies
     */
    @Test
    public void testValidProfBasicViews_DuplicateFunctionalDependency() throws MetadataExtractionException, FileNotFoundException {
        ImmutableSet<OntopViewDefinition> viewDefinitions = loadViewDefinitions("/prof/prof-basic-views-with-constraints-duplicateFD.json", "/prof/prof_with_constraints.db-extract.json");
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
                new FileReader(ViewDefinitionParsingTest.class.getResource(dbMetadataFilePath).getPath()));

        OntopViewMetadataProvider viewMetadataProvider = viewMetadataProviderFactory.getMetadataProvider(dbMetadataProvider,
                new FileReader(ViewDefinitionParsingTest.class.getResource(viewFilePath).getPath()));

        ImmutableMetadata metadata = ImmutableMetadata.extractImmutableMetadata(viewMetadataProvider);

        return metadata.getAllRelations().stream()
                .filter(r -> r instanceof OntopViewDefinition)
                .map(r -> (OntopViewDefinition) r)
                .collect(ImmutableCollectors.toSet());
    }

}
