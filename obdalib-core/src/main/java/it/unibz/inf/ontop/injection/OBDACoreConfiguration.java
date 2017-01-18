package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.OBDACoreConfigurationImpl;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;

/**
 * TODO: explain
 */
public interface OBDACoreConfiguration extends OntopMappingSQLConfiguration {

    @Override
    OBDASettings getSettings();

    /**
     * Default builder
     */
    static Builder<? extends Builder> defaultBuilder() {
        return new OBDACoreConfigurationImpl.BuilderImpl<>();
    }

    /**
     * TODO: explain
     */
    interface OBDACoreBuilderFragment<B extends Builder<B>> {

        B nativeOntopMappingFile(@Nonnull File mappingFile);

        B nativeOntopMappingFile(@Nonnull String mappingFilename);

        B nativeOntopMappingReader(@Nonnull Reader mappingReader);

        B r2rmlMappingFile(@Nonnull File mappingFile);

        B r2rmlMappingFile(@Nonnull String mappingFilename);

        B r2rmlMappingReader(@Nonnull Reader mappingReader);

        B r2rmlMappingGraph(@Nonnull Model rdfGraph);

    }

    interface Builder<B extends Builder<B>> extends OBDACoreBuilderFragment<B>, OntopMappingSQLConfiguration.Builder<B> {

        @Override
        OBDACoreConfiguration build();
    }
}
