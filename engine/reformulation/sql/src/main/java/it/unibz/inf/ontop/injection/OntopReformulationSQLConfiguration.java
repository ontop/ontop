package it.unibz.inf.ontop.injection;


public interface OntopReformulationSQLConfiguration extends OntopReformulationConfiguration, OntopSQLCoreConfiguration {

    @Override
    OntopReformulationSQLSettings getSettings();

    interface OntopReformulationSQLBuilderFragment<B extends OntopReformulationSQLConfiguration.Builder<B>> {
    }

    interface Builder<B extends OntopReformulationSQLConfiguration.Builder<B>>
            extends OntopReformulationSQLBuilderFragment<B>,
            OntopReformulationConfiguration.Builder<B>, OntopSQLCoreConfiguration.Builder<B> {

        @Override
        OntopReformulationSQLConfiguration build();
    }
}
