package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.impl.OntopMappingSQLConfigurationImpl;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface OntopMappingSQLConfiguration extends OntopSQLCredentialConfiguration, OntopMappingConfiguration {

    @Override
    OntopMappingSQLSettings getSettings();

    /**
     * Default builder
     */
    static Builder<? extends Builder<?>> defaultBuilder() {
        return new OntopMappingSQLConfigurationImpl.BuilderImpl<>();
    }

    Optional<SQLPPMapping> loadPPMapping() throws MappingException;

    default SQLPPMapping loadProvidedPPMapping() throws MappingException {
        return loadPPMapping()
                .orElseThrow(() -> new IllegalStateException("No PreProcessedMapping could have been loaded. " +
                        "Do not call this method unless you are sure of the input provision."));
    }

    /**
     * TODO: explain
     */
    interface OntopMappingSQLBuilderFragment<B extends Builder<B>> {

        B ppMapping(@Nonnull SQLPPMapping ppMapping);
    }

    interface Builder<B extends Builder<B>> extends OntopMappingSQLBuilderFragment<B>,
            OntopSQLCredentialConfiguration.Builder<B>, OntopMappingConfiguration.Builder<B> {

        @Override
        OntopMappingSQLConfiguration build();
    }
}


