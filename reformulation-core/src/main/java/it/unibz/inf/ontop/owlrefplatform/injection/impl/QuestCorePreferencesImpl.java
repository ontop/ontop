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
    public List<String> getReformulationPlatformPreferencesKeys(){
        ArrayList<String> keys = new ArrayList<String>();
        keys.add(REFORMULATION_TECHNIQUE);
        keys.add(ABOX_MODE);
        keys.add(DBTYPE);
//		keys.add(DATA_LOCATION);
        keys.add(OBTAIN_FROM_ONTOLOGY);
        keys.add(OBTAIN_FROM_MAPPINGS);
        keys.add(OPTIMIZE_EQUIVALENCES);
//		keys.add(ANNOTATIONS_IN_ONTO);
//		keys.add(OPTIMIZE_TBOX_SIGMA);
//		keys.add(CREATE_TEST_MAPPINGS);

        return keys;
    }

    @Override
    public boolean isOntologyAnnotationQueryingEnabled() {
        return getBoolean(ANNOTATIONS_IN_ONTO)
                .orElseThrow(() -> new IllegalStateException("ANNOTATIONS_IN_ONTO must have a default value"));
    }

    @Override
    public boolean isSameAsInMappingsEnabled() {
        return getBoolean(SAME_AS)
                .orElseThrow(() -> new IllegalStateException("SAME_AS must have a default value"));
    }

    @Override
    public boolean isRewritingEnabled() {
        return getBoolean(REWRITE)
                .orElseThrow(() -> new IllegalStateException("REWRITE must have a default value"));
    }

    @Override
    public boolean isEquivalenceOptimizationEnabled() {
        return getBoolean(OPTIMIZE_EQUIVALENCES)
                .orElseThrow(() -> new IllegalStateException("OPTIMIZE_EQUIVALENCES must have a default value"));
    }

    @Override
    public boolean isKeyPrintingEnabled() {
        return getBoolean(PRINT_KEYS)
                .orElseThrow(() -> new IllegalStateException("PRINT_KEYS must have a default value"));
    }

    @Override
    public boolean isDistinctPostProcessingEnabled() {
        return getBoolean(DISTINCT_RESULTSET)
                .orElseThrow(() -> new IllegalStateException("DISTINCT_RESULTSET must have a default value"));
    }

    @Override
    public boolean isIRISafeEncodingEnabled() {
        return getBoolean(SQL_GENERATE_REPLACE)
                .orElseThrow(() -> new IllegalStateException("SQL_GENERATE_REPLACE must have a default value"));
    }

    @Override
    public boolean isInVirtualMode() {
        String mode =  getProperty(ABOX_MODE)
                .orElseThrow(() -> new IllegalStateException("ABOX_MODE must have a default value"));

        return mode.equals(QuestConstants.VIRTUAL);
    }

    @Override
    public boolean isKeepAliveEnabled() {
        return getBoolean(KEEP_ALIVE)
                .orElseThrow(() -> new IllegalStateException("KEEP_ALIVE must have a default value"));
    }

    @Override
    public boolean isRemoveAbandonedEnabled() {
        return getBoolean(REMOVE_ABANDONED)
                .orElseThrow(() -> new IllegalStateException("REMOVE_ABANDONED must have a default value"));
    }

    @Override
    public int getAbandonedTimeout() {
        return getInteger(ABANDONED_TIMEOUT)
                .orElseThrow(() -> new IllegalStateException("ABANDONED_TIMEOUT must have a default value"));
    }

    @Override
    public int getConnectionPoolInitialSize() {
        return getInteger(INIT_POOL_SIZE)
                .orElseThrow(() -> new IllegalStateException("INIT_POOL_SIZE must have a default value"));
    }

    @Override
    public int getConnectionPoolMaxSize() {
        return getInteger(MAX_POOL_SIZE)
                .orElseThrow(() -> new IllegalStateException("MAX_POOL_SIZE must have a default value"));
    }
}
