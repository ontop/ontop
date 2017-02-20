package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;

public class OntopSQLCommonModule extends OntopAbstractModule {

    private final OntopSQLCoreSettings settings;

    protected OntopSQLCommonModule(OntopSQLCoreConfiguration configuration) {
        super(configuration.getSettings());
        this.settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopSQLCoreSettings.class).toInstance(settings);
    }
}
