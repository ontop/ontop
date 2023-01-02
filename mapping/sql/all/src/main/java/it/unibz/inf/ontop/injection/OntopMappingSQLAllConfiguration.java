package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.OntopMappingSQLAllConfigurationImpl;
import org.apache.commons.rdf.api.Graph;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;

/**
 * TODO: find a better name
 */
public interface OntopMappingSQLAllConfiguration extends OntopMappingSQLConfiguration {

    @Override
    OntopMappingSQLAllSettings getSettings();

    static <B extends Builder<B>> Builder<B> defaultBuilder() {
        return new OntopMappingSQLAllConfigurationImpl.BuilderImpl<>();
    }

    interface OntopMappingSQLAllBuilderFragment<B extends Builder<B>> {

        B nativeOntopMappingFile(@Nonnull File mappingFile);

        B nativeOntopMappingFile(@Nonnull String mappingFilename);

        B nativeOntopMappingReader(@Nonnull Reader mappingReader);

        B r2rmlMappingFile(@Nonnull File mappingFile);

        B r2rmlMappingFile(@Nonnull String mappingFilename);

        B r2rmlMappingReader(@Nonnull Reader mappingReader);

        B r2rmlMappingGraph(@Nonnull Graph rdfGraph);

        B basicImplicitConstraintFile(@Nonnull File constraintFile);

        B basicImplicitConstraintFile(@Nonnull String constraintFilename);

        B dbMetadataFile(@Nonnull File dbMetadataFile);

        B dbMetadataFile(@Nonnull String dbMetadataFilename);

        B dbMetadataReader(@Nonnull Reader dbMetadataReader);

        B lensesFile(@Nonnull File ontopViewFile);

        @Deprecated
        default B ontopViewFile(@Nonnull String lensesFilename) {
            return lensesFile(lensesFilename);
        }

        B lensesFile(@Nonnull String lensesFilename);

        @Deprecated
        default B ontopViewReader(@Nonnull Reader lensesReader) {
            return lensesReader(lensesReader);
        }

        B lensesReader(@Nonnull Reader lensesReader);
    }

    interface Builder<B extends Builder<B>> extends OntopMappingSQLConfiguration.Builder<B>,
            OntopMappingSQLAllBuilderFragment<B> {

        @Override
        OntopMappingSQLAllConfiguration build();
    }
}
