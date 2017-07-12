package it.unibz.inf.ontop.injection;


public interface OntopSystemSQLConfiguration extends OntopSystemConfiguration, OntopTranslationSQLConfiguration {

    @Override
    OntopSystemSQLSettings getSettings();

}
