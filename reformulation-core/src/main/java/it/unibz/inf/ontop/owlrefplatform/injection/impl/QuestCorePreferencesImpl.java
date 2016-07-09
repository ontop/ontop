package it.unibz.inf.ontop.owlrefplatform.injection.impl;


import it.unibz.inf.ontop.injection.impl.OBDAPropertiesImpl;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCorePreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class QuestCorePreferencesImpl extends OBDAPropertiesImpl implements QuestCorePreferences {

    private static final String DEFAULT_QUEST_PROPERTIES_FILE = "QuestDefaults.properties";

    /**
     * Recommended constructor.
     *
     * Beware: immutable class!
     *
     * Changing the Properties object afterwards will not have any effect
     * on this OBDAProperties object.
     */
    protected QuestCorePreferencesImpl(Properties userPreferences, boolean isR2rml) {
        super(loadQuestPreferences(userPreferences), isR2rml);
    }


    private static Properties loadQuestPreferences(Properties userPreferences) {
        Properties properties = loadDefaultPropertiesFromFile(QuestCorePreferences.class, DEFAULT_QUEST_PROPERTIES_FILE);
        properties.putAll(userPreferences);
        return properties;
    }

    @Override
    public boolean isOntologyAnnotationQueryingEnabled() {
        return getRequiredBoolean(ANNOTATIONS_IN_ONTO);
    }

    @Override
    public boolean isSameAsInMappingsEnabled() {
        return getRequiredBoolean(SAME_AS);
    }

    @Override
    public boolean isRewritingEnabled() {
        return getRequiredBoolean(REWRITE);
    }

    @Override
    public boolean isEquivalenceOptimizationEnabled() {
        return getRequiredBoolean(OPTIMIZE_EQUIVALENCES);
    }

    @Override
    public boolean isKeyPrintingEnabled() {
        return getRequiredBoolean(PRINT_KEYS);
    }

    @Override
    public boolean isDistinctPostProcessingEnabled() {
        return getRequiredBoolean(DISTINCT_RESULTSET);
    }

    @Override
    public boolean isIRISafeEncodingEnabled() {
        return getRequiredBoolean(SQL_GENERATE_REPLACE);
    }

    @Override
    public boolean isInVirtualMode() {
        String mode = getRequiredProperty(ABOX_MODE);

        return mode.equals(QuestConstants.VIRTUAL);
    }

    @Override
    public boolean isKeepAliveEnabled() {
        return getRequiredBoolean(KEEP_ALIVE);
    }

    @Override
    public boolean isRemoveAbandonedEnabled() {
        return getRequiredBoolean(REMOVE_ABANDONED);
    }

    @Override
    public int getAbandonedTimeout() {
        return getRequiredInteger(ABANDONED_TIMEOUT);
    }

    @Override
    public int getConnectionPoolInitialSize() {
        return getRequiredInteger(INIT_POOL_SIZE);
    }

    @Override
    public int getConnectionPoolMaxSize() {
        return getRequiredInteger(MAX_POOL_SIZE);
    }
}
