package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.OntopMappingSQLTemporalConfigurationImpl;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;

public interface OntopMappingSQLTemporalConfiguration extends OntopMappingSQLAllConfiguration {

    @Override
    OntopMappingSQLAllSettings getSettings();

    static Builder<? extends OntopMappingSQLTemporalConfiguration.Builder> defaultBuilder() {
        return new OntopMappingSQLTemporalConfigurationImpl.BuilderImpl<>();
    }

    interface OntopMappingSQLTemporalBuilderFragment<B extends OntopMappingSQLTemporalConfiguration.Builder<B>> extends OntopMappingSQLAllBuilderFragment<B> {

        B nativeOntopTemporalMappingFile(@Nonnull File mappingFile);

        B nativeOntopTemporalMappingFile(@Nonnull String mappingFilename);

        B nativeOntopTemporalMappingReader(@Nonnull Reader mappingReader);
    }

    interface Builder<B extends Builder<B>> extends OntopMappingSQLAllConfiguration.Builder<B>,
            OntopMappingSQLTemporalConfiguration.OntopMappingSQLTemporalBuilderFragment<B> {

        @Override
        OntopMappingSQLTemporalConfiguration build();
    }
}
