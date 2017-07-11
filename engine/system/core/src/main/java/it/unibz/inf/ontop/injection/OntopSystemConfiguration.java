package it.unibz.inf.ontop.injection;


public interface OntopSystemConfiguration extends OntopTranslationConfiguration, OntopOBDASpecificationConfiguration {

    @Override
    OntopSystemSettings getSettings();
}
