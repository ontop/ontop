package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopSQLCredentialConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;

public class OntopSQLCredentialModule extends OntopAbstractModule {

    private final OntopSQLCredentialSettings settings;

    protected OntopSQLCredentialModule(OntopSQLCredentialConfiguration configuration) {
        super(configuration.getSettings());
        this.settings = configuration.getSettings();
    }

    @Override
    protected void configure() {
        bind(OntopSQLCredentialSettings.class).toInstance(settings);
    }
}
