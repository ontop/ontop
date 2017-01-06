package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.QuestPreferences;

import java.util.Optional;
import java.util.Properties;

public class QuestPreferencesImpl extends QuestCorePreferencesImpl implements QuestPreferences {

    protected QuestPreferencesImpl(Properties userPreferences, boolean isR2rml) {
        super(userPreferences, isR2rml);
    }

    @Override
    public Optional<String> getOntologyURL() {
        return getProperty(ONTOLOGY_URL);
    }
}
