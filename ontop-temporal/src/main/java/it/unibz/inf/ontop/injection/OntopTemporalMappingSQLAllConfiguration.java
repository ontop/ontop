package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.OntopTemporalMappingSQLAllConfigurationImpl;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;

public interface OntopTemporalMappingSQLAllConfiguration extends OntopMappingSQLAllConfiguration {

    static Builder<? extends Builder> defaultBuilder() {
        return new OntopTemporalMappingSQLAllConfigurationImpl.BuilderImpl<>();
    }

    interface OntopTemporalMappingSQLAllBuilderFragment<B extends Builder<B>> extends OntopMappingSQLAllBuilderFragment<B> {

        B nativeOntopTemporalMappingFile(@Nonnull File mappingFile);

        B nativeOntopTemporalMappingFile(@Nonnull String mappingFilename);

        B nativeOntopTemporalMappingReader(@Nonnull Reader mappingReader);

        B enableTemporalMode();
    }

    interface Builder<B extends Builder<B>> extends OntopMappingSQLAllConfiguration.Builder<B>,
            OntopTemporalMappingSQLAllBuilderFragment<B> {

        @Override
        OntopTemporalMappingSQLAllConfiguration build();
    }
}
