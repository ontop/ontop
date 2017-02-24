package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopQueryAnsweringSQLSettings;
import it.unibz.inf.ontop.injection.OntopStandaloneSQLSettings;

import java.util.Properties;


public class OntopStandaloneSQLSettingsImpl extends OntopMappingSQLAllSettingsImpl implements OntopStandaloneSQLSettings {

    private final OntopQueryAnsweringSQLSettings qaSettings;

    OntopStandaloneSQLSettingsImpl(Properties userProperties, boolean isR2rml) {
        super(loadProperties(userProperties), isR2rml);
        qaSettings = new OntopQueryAnsweringSQLSettingsImpl(copyProperties());
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = new OntopQueryAnsweringSQLSettingsImpl(userProperties).copyProperties();
        properties.putAll(userProperties);
        return properties;
    }

    @Override
    public boolean isExistentialReasoningEnabled() {
        return qaSettings.isExistentialReasoningEnabled();
    }

    @Override
    public boolean isDistinctPostProcessingEnabled() {
        return qaSettings.isDistinctPostProcessingEnabled();
    }

    @Override
    public boolean isIRISafeEncodingEnabled() {
        return qaSettings.isIRISafeEncodingEnabled();
    }

    @Override
    public boolean isKeepAliveEnabled() {
        return qaSettings.isKeepAliveEnabled();
    }

    @Override
    public boolean isRemoveAbandonedEnabled() {
        return qaSettings.isRemoveAbandonedEnabled();
    }

    @Override
    public int getAbandonedTimeout() {
        return qaSettings.getAbandonedTimeout();
    }

    @Override
    public int getConnectionPoolInitialSize() {
        return qaSettings.getConnectionPoolInitialSize();
    }

    @Override
    public int getConnectionPoolMaxSize() {
        return qaSettings.getConnectionPoolMaxSize();
    }
}
