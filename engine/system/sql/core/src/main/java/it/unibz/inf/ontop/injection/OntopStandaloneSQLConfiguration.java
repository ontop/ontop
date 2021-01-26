package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.injection.impl.OntopStandaloneSQLConfigurationImpl;

/**
 * Standalone: the configuration contains information both for extracting the OBDA specification and for query answering.
 */
public interface OntopStandaloneSQLConfiguration extends OntopSystemSQLConfiguration, OntopMappingSQLAllConfiguration {

    @Override
    OntopStandaloneSQLSettings getSettings();

    static Builder<? extends Builder<?>> defaultBuilder() {
        return new OntopStandaloneSQLConfigurationImpl.BuilderImpl<>();
    }

    interface Builder<B extends Builder<B>> extends OntopSystemConfiguration.Builder<B>,
            OntopSystemSQLConfiguration.Builder<B>,
            OntopMappingSQLAllConfiguration.Builder<B> {

        @Override
        OntopStandaloneSQLConfiguration build();
    }

}
