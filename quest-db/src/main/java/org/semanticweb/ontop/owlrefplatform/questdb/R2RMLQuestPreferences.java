package org.semanticweb.ontop.owlrefplatform.questdb;

import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;

import java.util.Properties;

/**
 * Convenient class, should not be required nor expected.
 * It just saves people from declaring manually the default classes for parsing R2RML files.
 *
 * Needed class: R2RMLMappingParser.
 *
 * As usual, default entries can be overloaded by the user.
 *
 */
public class R2RMLQuestPreferences extends QuestPreferences {

    private static final String DEFAULT_R2RML_PROPERTIES_FILE = "R2RMLDefaults.properties";

    public R2RMLQuestPreferences() {
        this(new Properties());
    }

    public R2RMLQuestPreferences(Properties userProperties) {
        super(loadR2RMLDefaultProperties(userProperties));
    }

    private static Properties loadR2RMLDefaultProperties(Properties userPreferences) {
        Properties properties = loadDefaultPropertiesFromFile(R2RMLQuestPreferences.class, DEFAULT_R2RML_PROPERTIES_FILE);
        properties.putAll(userPreferences);
        return properties;
    }

}
