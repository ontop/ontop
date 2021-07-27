package it.unibz.inf.ontop.injection.impl;

import java.util.Properties;

import it.unibz.inf.ontop.injection.OntopCTablesSettings;

public class OntopCTablesSettingsImpl extends OntopSQLCredentialSettingsImpl
        implements OntopCTablesSettings {

    private final String ctablesRulesetFile;

    protected OntopCTablesSettingsImpl(final Properties userProperties) {
        super(userProperties);
        this.ctablesRulesetFile = getRequiredProperty(OntopCTablesSettings.CTABLES_RULESET_FILE);
    }

    @Override
    public String getCTablesRulesetFile() {
        return this.ctablesRulesetFile;
    }

}
