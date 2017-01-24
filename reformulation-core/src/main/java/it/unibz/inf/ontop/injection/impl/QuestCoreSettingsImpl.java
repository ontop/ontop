package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopRuntimeSettings;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.injection.QuestCoreSettings;

import java.util.Optional;
import java.util.Properties;

public class QuestCoreSettingsImpl extends OBDASettingsImpl implements QuestCoreSettings {

    private static final String DEFAULT_QUEST_PROPERTIES_FILE = "QuestDefaults.properties";
    private final OntopRuntimeSettings runtimeSettings;

    /**
     * Recommended constructor.
     *
     * Beware: immutable class!
     *
     * Changing the Properties object afterwards will not have any effect
     * on this OBDAProperties object.
     */
    protected QuestCoreSettingsImpl(Properties userPreferences, boolean isR2rml) {
        super(loadQuestPreferences(userPreferences), isR2rml);
        runtimeSettings = new OntopRuntimeSettingsImpl(copyProperties());
    }

    private static Properties loadQuestPreferences(Properties userPreferences) {
        Properties properties = OntopOptimizationSettingsImpl.loadDefaultOptimizationProperties();
        properties.putAll(OntopRuntimeSettingsImpl.loadDefaultRuntimeProperties());
        properties.putAll(loadDefaultQuestCorePreferences());
        properties.putAll(userPreferences);
        return properties;
    }

    public static Properties loadDefaultQuestCorePreferences() {
        return loadDefaultPropertiesFromFile(QuestCoreSettings.class, DEFAULT_QUEST_PROPERTIES_FILE);
    }

    @Override
    public boolean isKeyPrintingEnabled() {
        return getRequiredBoolean(PRINT_KEYS);
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

    @Override
    public Optional<Boolean> getBoolean(String key) {
        return super.getBoolean(key);
    }

    @Override
    public boolean getRequiredBoolean(String key) {
        return super.getRequiredBoolean(key);
    }

    @Override
    public Optional<String> getProperty(String key) {
        return super.getProperty(key);
    }

    @Override
    public String getRequiredProperty(String key) {
        return super.getRequiredProperty(key);
    }

    @Override
    public boolean isRewritingEnabled() {
        return runtimeSettings.isRewritingEnabled();
    }

    @Override
    public boolean isDistinctPostProcessingEnabled() {
        return runtimeSettings.isDistinctPostProcessingEnabled();
    }

    @Override
    public boolean isIRISafeEncodingEnabled() {
        return runtimeSettings.isIRISafeEncodingEnabled();
    }
}
