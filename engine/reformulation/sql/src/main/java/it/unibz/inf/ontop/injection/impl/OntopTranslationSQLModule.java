package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopReformulationSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopReformulationSQLSettings;

public class OntopTranslationSQLModule extends OntopAbstractModule {

    private final OntopReformulationSQLSettings settings;

    protected OntopTranslationSQLModule(OntopReformulationSQLConfiguration configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopReformulationSQLSettings.class).toInstance(settings);
    }
}
