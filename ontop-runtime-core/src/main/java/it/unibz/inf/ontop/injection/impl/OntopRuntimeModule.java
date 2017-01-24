package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopRuntimeConfiguration;
import it.unibz.inf.ontop.injection.OntopRuntimeSettings;

public class OntopRuntimeModule extends OntopAbstractModule {

    private final OntopRuntimeSettings settings;

    protected OntopRuntimeModule(OntopRuntimeConfiguration configuration) {
        super(configuration.getSettings());
        settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopRuntimeSettings.class).toInstance(settings);
    }
}
