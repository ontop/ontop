package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopSystemSettings;

public class OntopSystemModule extends OntopAbstractModule {

    private final OntopSystemSettings settings;

    protected OntopSystemModule(OntopSystemSettings settings) {
        super(settings);
        this.settings = settings;
    }

    @Override
    protected void configure() {
        bind(OntopSystemSettings.class).toInstance(settings);
    }
}
