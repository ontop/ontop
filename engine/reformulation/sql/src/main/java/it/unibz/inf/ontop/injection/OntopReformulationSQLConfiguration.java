package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.injection.impl.OntopReformulationSQLConfigurationImpl;

public interface OntopReformulationSQLConfiguration extends OntopReformulationConfiguration, OntopSQLCoreConfiguration {

    @Override
    OntopReformulationSQLSettings getSettings();

    /**
     * This builder will require a OBDA specification to be directly assigned
     */
    static OntopReformulationSQLConfiguration.Builder<? extends Builder<?>> defaultBuilder() {
        return new OntopReformulationSQLConfigurationImpl.BuilderImpl<>();
    }

    interface OntopReformulationSQLBuilderFragment<B extends OntopReformulationSQLConfiguration.Builder<B>> {
    }

    interface Builder<B extends OntopReformulationSQLConfiguration.Builder<B>>
            extends OntopReformulationSQLBuilderFragment<B>,
            OntopReformulationConfiguration.Builder<B>, OntopSQLCoreConfiguration.Builder<B> {

        @Override
        OntopReformulationSQLConfiguration build();
    }
}
