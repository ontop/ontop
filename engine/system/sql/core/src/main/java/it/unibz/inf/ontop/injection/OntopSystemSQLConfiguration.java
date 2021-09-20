package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.injection.impl.OntopSystemSQLConfigurationImpl;

public interface OntopSystemSQLConfiguration extends OntopSystemConfiguration, OntopReformulationSQLConfiguration,
        OntopSQLCredentialConfiguration {

    @Override
    OntopSystemSQLSettings getSettings();

    /**
     * This builder will require a OBDA specification to be directly assigned
     */
    static Builder<? extends Builder<?>> defaultBuilder() {
        return new OntopSystemSQLConfigurationImpl.BuilderImpl<>();
    }

    interface Builder<B extends Builder<B>> extends OntopReformulationSQLConfiguration.Builder<B>,
            OntopSQLCredentialConfiguration.Builder<B> {

        @Override
        OntopSystemSQLConfiguration build();
    }

}
