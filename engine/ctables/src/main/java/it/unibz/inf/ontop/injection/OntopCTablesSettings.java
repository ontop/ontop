package it.unibz.inf.ontop.injection;

public interface OntopCTablesSettings extends OntopSQLCredentialSettings {

    String getCTablesRulesetFile();

    // -------
    // Keys
    // -------

    String CTABLES_RULESET_FILE = "ctables.ruleset";

}
