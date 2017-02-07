package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.OntopStandaloneSQLConfigurationImpl;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;

/**
 * Standalone: the configuration contains information both for extracting the OBDA specification and for query answering.
 */
public interface OntopStandaloneSQLConfiguration extends OntopSystemSQLConfiguration, OntopMappingSQLConfiguration {

    @Override
    OntopStandaloneSQLSettings getSettings();

    static Builder<? extends Builder> defaultBuilder() {
        return new OntopStandaloneSQLConfigurationImpl.BuilderImpl<>();
    }

    interface OntopStandaloneSQLConfigurationBuilderFragment<B extends Builder<B>> {

        B nativeOntopMappingFile(@Nonnull File mappingFile);

        B nativeOntopMappingFile(@Nonnull String mappingFilename);

        B nativeOntopMappingReader(@Nonnull Reader mappingReader);

        B r2rmlMappingFile(@Nonnull File mappingFile);

        B r2rmlMappingFile(@Nonnull String mappingFilename);

        B r2rmlMappingReader(@Nonnull Reader mappingReader);

        B r2rmlMappingGraph(@Nonnull Model rdfGraph);

    }

    interface Builder<B extends Builder<B>> extends OntopQueryAnsweringSQLConfiguration.Builder<B>,
            OntopStandaloneSQLConfigurationBuilderFragment<B>,
            OntopMappingSQLConfiguration.Builder<B> {

        @Override
        OntopStandaloneSQLConfiguration build();
    }

}
