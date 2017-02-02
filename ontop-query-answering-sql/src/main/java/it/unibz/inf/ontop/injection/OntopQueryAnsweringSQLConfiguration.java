package it.unibz.inf.ontop.injection;


public interface OntopQueryAnsweringSQLConfiguration extends OntopQueryAnsweringConfiguration, OntopSQLCoreConfiguration {

    @Override
    OntopQueryAnsweringSQLSettings getSettings();

    interface OntopQueryAnsweringSQLBuilderFragment<B extends OntopQueryAnsweringSQLConfiguration.Builder<B>> {
    }

    interface Builder<B extends OntopQueryAnsweringSQLConfiguration.Builder<B>>
            extends OntopQueryAnsweringSQLConfiguration.OntopQueryAnsweringSQLBuilderFragment<B>,
            OntopSQLCoreConfiguration.Builder<B> {

        @Override
        OntopQueryAnsweringSQLConfiguration build();
    }
}
