package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopQueryAnsweringSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopQueryAnsweringSQLSettings;

public class OntopQueryAnsweringSQLModule extends OntopAbstractModule {

    private final OntopQueryAnsweringSQLSettings settings;

    protected OntopQueryAnsweringSQLModule(OntopQueryAnsweringSQLConfiguration configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopQueryAnsweringSQLSettings.class).toInstance(settings);
    }
}
