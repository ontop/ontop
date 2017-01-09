package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.QuestSettings;

import java.util.Optional;
import java.util.Properties;

public class QuestSettingsImpl extends QuestCoreSettingsImpl implements QuestSettings {

    protected QuestSettingsImpl(Properties userPreferences, boolean isR2rml) {
        super(userPreferences, isR2rml);
    }

    @Override
    public Optional<String> getOntologyURL() {
        return getProperty(ONTOLOGY_URL);
    }
}
