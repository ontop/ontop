package it.unibz.inf.ontop.injection.impl;

import java.util.Properties;

import javax.annotation.Nullable;

import it.unibz.inf.ontop.injection.OntopCTablesSettings;

public class OntopCTablesSettingsImpl extends OntopSQLCredentialSettingsImpl
        implements OntopCTablesSettings {

    @Nullable
    private final String ctablesRulesetFile; // null if ruleset object manually injected

    @Nullable
    private final String ctablesRefreshSchedule;

    protected OntopCTablesSettingsImpl(final Properties userProperties) {
        super(userProperties);
        this.ctablesRulesetFile = getProperty(OntopCTablesSettings.CTABLES_RULESET_FILE)
                .orElse(null);
        this.ctablesRefreshSchedule = getProperty(OntopCTablesSettings.CTABLES_REFRESH_SCHEDULE)
                .orElse(null);
    }

    @Override
    @Nullable
    public String getCTablesRulesetFile() {
        return this.ctablesRulesetFile;
    }

    @Override
    @Nullable
    public String getCTablesRefreshSchedule() {
        return this.ctablesRefreshSchedule;
    }

}
