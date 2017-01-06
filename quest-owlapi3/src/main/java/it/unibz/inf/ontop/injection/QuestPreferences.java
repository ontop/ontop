package it.unibz.inf.ontop.injection;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface QuestPreferences extends QuestCorePreferences {

    Optional<String> getOntologyURL();

    String ONTOLOGY_URL = "ONTOLOGY_URL";
}
