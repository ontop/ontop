package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.impl.OBDACoreConfigurationImpl;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;
import org.eclipse.rdf4j.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

/**
 * TODO: explain
 */
public interface OBDACoreConfiguration extends OntopMappingSQLConfiguration {

    @Override
    OBDASettings getSettings();

    Optional<ImplicitDBConstraintsReader> getImplicitDBConstraintsReader();

    /**
     * TODO: explain
     */
    Optional<OBDAModel> loadMapping() throws IOException, InvalidMappingException;

    /**
     * Only call it if you are sure that mapping assertions have been provided
     */
    default OBDAModel loadProvidedMapping() throws IOException, InvalidMappingException {
        return loadMapping()
                .orElseThrow(() -> new IllegalStateException("No mapping has been provided. " +
                        "Do not call this method unless you are sure of the mapping provision."));
    }

    /**
     * Default builder
     */
    static Builder<Builder<Builder<Builder<Builder<Builder<Builder>>>>>> defaultBuilder() {
        return new OBDACoreConfigurationImpl.BuilderImpl<>();
    }

    /**
     * TODO: explain
     */
    interface OBDACoreBuilderFragment<B extends Builder> {

        B obdaModel(@Nonnull OBDAModel obdaModel);

        B nativeOntopMappingFile(@Nonnull File mappingFile);

        B nativeOntopMappingFile(@Nonnull String mappingFilename);

        B nativeOntopMappingReader(@Nonnull Reader mappingReader);

        B r2rmlMappingFile(@Nonnull File mappingFile);

        B r2rmlMappingFile(@Nonnull String mappingFilename);

        B r2rmlMappingReader(@Nonnull Reader mappingReader);

        B r2rmlMappingGraph(@Nonnull Model rdfGraph);

        B dbConstraintsReader(@Nonnull ImplicitDBConstraintsReader constraints);

    }

    interface Builder<B extends Builder> extends OBDACoreBuilderFragment<B>, OntopMappingSQLConfiguration.Builder<B> {

        @Override
        OBDACoreConfiguration build();
    }
}
