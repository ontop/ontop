package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.injection.impl.QuestCorePreferencesImpl;

import java.util.Optional;
import java.util.Properties;

public class QuestPreferencesImpl extends QuestCorePreferencesImpl implements QuestPreferences {

    protected QuestPreferencesImpl(Properties userPreferences, boolean isR2rml) {
        super(userPreferences, isR2rml);
    }

    @Override
    public Optional<String> getOntologyFilePath() {
        return getProperty(QuestPreferences.ONTOLOGY_FILE_PATH);
    }
}
