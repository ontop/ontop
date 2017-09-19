package it.unibz.inf.ontop.injection;


public interface OntopReformulationSQLConfiguration extends OntopReformulationConfiguration, OntopSQLCoreConfiguration {

    @Override
    OntopReformulationSQLSettings getSettings();

    interface OntopQueryAnsweringSQLBuilderFragment<B extends OntopReformulationSQLConfiguration.Builder<B>> {
    }

    interface Builder<B extends OntopReformulationSQLConfiguration.Builder<B>>
            extends OntopReformulationSQLConfiguration.OntopQueryAnsweringSQLBuilderFragment<B>,
            OntopReformulationConfiguration.Builder<B>, OntopSQLCoreConfiguration.Builder<B> {

        @Override
        OntopReformulationSQLConfiguration build();
    }
}
