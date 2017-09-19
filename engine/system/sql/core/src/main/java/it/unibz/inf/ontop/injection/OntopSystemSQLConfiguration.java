package it.unibz.inf.ontop.injection;


public interface OntopSystemSQLConfiguration extends OntopSystemConfiguration, OntopReformulationSQLConfiguration {

    @Override
    OntopSystemSQLSettings getSettings();

}
