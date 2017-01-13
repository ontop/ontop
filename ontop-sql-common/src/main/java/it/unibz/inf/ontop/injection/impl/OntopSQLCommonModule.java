package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopSQLConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLSettings;

public class OntopSQLCommonModule extends OntopAbstractModule {

    private final OntopSQLSettings settings;

    protected OntopSQLCommonModule(OntopSQLConfiguration configuration) {
        super(configuration.getSettings());
        this.settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopSQLSettings.class).toInstance(settings);
    }
}
