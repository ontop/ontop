package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.OntopMappingSQLAllConfigurationImpl;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;

/**
 * TODO: find a better name
 */
public interface OntopMappingSQLAllConfiguration extends OntopMappingSQLConfiguration {

    @Override
    OntopMappingSQLAllSettings getSettings();

    static Builder<? extends Builder> defaultBuilder() {
        return new OntopMappingSQLAllConfigurationImpl.BuilderImpl<>();
    }

    interface OntopMappingSQLAllBuilderFragment<B extends Builder<B>> {

        B nativeOntopMappingFile(@Nonnull File mappingFile);

        B nativeOntopMappingFile(@Nonnull String mappingFilename);

        B nativeOntopMappingReader(@Nonnull Reader mappingReader);

        B r2rmlMappingFile(@Nonnull File mappingFile);

        B r2rmlMappingFile(@Nonnull String mappingFilename);

        B r2rmlMappingReader(@Nonnull Reader mappingReader);

        B r2rmlMappingGraph(@Nonnull Model rdfGraph);
    }

    interface Builder<B extends Builder<B>> extends OntopMappingSQLConfiguration.Builder<B>,
            OntopMappingSQLAllBuilderFragment<B> {

        @Override
        OntopMappingSQLAllConfiguration build();
    }
}
