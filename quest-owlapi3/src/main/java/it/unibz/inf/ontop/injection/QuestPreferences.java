package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.owlrefplatform.injection.QuestCorePreferences;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface QuestPreferences extends QuestCorePreferences {

    Optional<String> getOntologyFilePath();

    String ONTOLOGY_FILE_PATH = "ONTOLOGY_FILE_PATH";
}
