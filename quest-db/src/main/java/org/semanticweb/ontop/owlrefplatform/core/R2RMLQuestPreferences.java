package org.semanticweb.ontop.owlrefplatform.core;

import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.r2rml.R2RMLMappingParser;

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

    public R2RMLQuestPreferences() {
        super(getDefaultR2RMLProperties());
    }

    public R2RMLQuestPreferences(Properties userProperties) {
        super(getR2RMLProperties(userProperties));
    }

    private static Properties getDefaultR2RMLProperties() {
        Properties p = new Properties();
        p.setProperty(MappingParser.class.getCanonicalName(), R2RMLMappingParser.class.getCanonicalName());
        return p;
    }

    private static Properties getR2RMLProperties(Properties userProperties) {
        Properties p = new Properties(getDefaultR2RMLProperties());
        p.putAll(userProperties);
        return p;
    }


}
