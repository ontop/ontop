package it.unibz.inf.ontop.injection;

import javax.annotation.Nullable;

public interface OntopCTablesSettings extends OntopSQLCredentialSettings {

    @Nullable
    String getCTablesRulesetFile();

    @Nullable
    String getCTablesRefreshSchedule();

    // -------
    // Keys
    // -------

    String CTABLES_RULESET_FILE = "ctables.ruleset";

    String CTABLES_REFRESH_SCHEDULE = "ctables.schedule";

}
