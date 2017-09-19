package it.unibz.inf.ontop.injection;


public interface OntopTranslationSQLConfiguration extends OntopTranslationConfiguration, OntopSQLCoreConfiguration {

    @Override
    OntopTranslationSQLSettings getSettings();

    interface OntopQueryAnsweringSQLBuilderFragment<B extends OntopTranslationSQLConfiguration.Builder<B>> {
    }

    interface Builder<B extends OntopTranslationSQLConfiguration.Builder<B>>
            extends OntopTranslationSQLConfiguration.OntopQueryAnsweringSQLBuilderFragment<B>,
            OntopTranslationConfiguration.Builder<B>, OntopSQLCoreConfiguration.Builder<B> {

        @Override
        OntopTranslationSQLConfiguration build();
    }
}
