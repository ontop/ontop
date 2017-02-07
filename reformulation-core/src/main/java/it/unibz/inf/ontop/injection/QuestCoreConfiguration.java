package it.unibz.inf.ontop.injection;

/**
 * TODO: explain
 *
 */
public interface QuestCoreConfiguration extends OBDACoreConfiguration, OntopQueryAnsweringConfiguration {

    @Override
    QuestCoreSettings getSettings();

}
