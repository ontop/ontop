package it.unibz.inf.ontop.injection;

import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.impl.OBDAConfigurationImpl;
import it.unibz.inf.ontop.io.InvalidDataSourceException;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;
import org.openrdf.model.Model;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import java.util.Properties;

/**
 * TODO: explain
 */
public interface OBDAConfiguration {

    OBDAProperties getOBDAProperties();

    Injector getInjector();

    Optional<ImplicitDBConstraintsReader> getImplicitDBConstraintsReader();

    /**
     * TODO: explain
     */
    Optional<OBDAModel> loadInputMappings() throws InvalidDataSourceException, IOException, InvalidMappingException;

    void validate() throws InvalidOBDAConfigurationException;

    /**
     * Default builder
     */
    static Builder<Builder> builder() {
        return new OBDAConfigurationImpl.BuilderImpl<>();
    }

    /**
     * TODO: explain
     */
    interface Builder<B extends Builder> {

        B obdaModel(@Nonnull OBDAModel obdaModel);

        B nativeOntopMappingFile(@Nonnull File mappingFile);

        B nativeOntopMappingFile(@Nonnull String mappingFilename);

        B nativeOntopMappingReader(@Nonnull Reader mappingReader);

        B r2rmlMappingFile(@Nonnull File mappingFile);

        B r2rmlMappingFile(@Nonnull String mappingFilename);

        B r2rmlMappingReader(@Nonnull Reader mappingReader);

        B r2rmlMappingGraph(@Nonnull Model rdfGraph);

        B properties(@Nonnull Properties properties);

        B dbConstraintsReader(@Nonnull ImplicitDBConstraintsReader constraints);

        B obtainFullMetadata(boolean obtainFullMetadata);

        B jdbcUrl(String jdbcUrl);

        OBDAConfiguration build();
    }
}
