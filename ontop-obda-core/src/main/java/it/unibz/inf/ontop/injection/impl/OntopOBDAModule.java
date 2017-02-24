package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopOBDAConfiguration;
import it.unibz.inf.ontop.injection.OntopOBDASettings;

class OntopOBDAModule extends OntopAbstractModule {

    private final OntopOBDASettings settings;

    protected OntopOBDAModule(OntopOBDAConfiguration configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {

        bind(OntopOBDASettings.class).toInstance(settings);
    }
}
