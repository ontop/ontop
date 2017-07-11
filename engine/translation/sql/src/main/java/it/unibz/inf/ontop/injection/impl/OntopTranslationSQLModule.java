package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopTranslationSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopTranslationSQLSettings;

public class OntopTranslationSQLModule extends OntopAbstractModule {

    private final OntopTranslationSQLSettings settings;

    protected OntopTranslationSQLModule(OntopTranslationSQLConfiguration configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopTranslationSQLSettings.class).toInstance(settings);
    }
}
