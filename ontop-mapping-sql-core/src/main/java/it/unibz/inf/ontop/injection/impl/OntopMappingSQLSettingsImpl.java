package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;

import java.util.Optional;
import java.util.Properties;


public class OntopMappingSQLSettingsImpl extends OntopMappingSettingsImpl implements OntopMappingSQLSettings {

    private static final String DEFAULT_PROPERTY_FILE = "mapping-sql-default.properties";
    private final OntopSQLCoreSettings sqlSettings;

    OntopMappingSQLSettingsImpl(Properties properties) {
        super(loadProperties(properties));
        sqlSettings = new OntopSQLCoreSettingsImpl(copyProperties());
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = OntopSQLCoreSettingsImpl.loadDefaultSQLProperties();
        properties.putAll(loadDefaultMappingSQLProperties());
        properties.putAll(userProperties);
        return properties;
    }

    public static Properties loadDefaultMappingSQLProperties() {
        return loadDefaultPropertiesFromFile(OntopMappingSQLSettings.class, DEFAULT_PROPERTY_FILE);
    }

    @Override
    public String getJdbcUrl() {
        return sqlSettings.getJdbcUrl();
    }

    @Override
    public String getJdbcName() {
        return sqlSettings.getJdbcName();
    }

    @Override
    public String getJdbcUser() {
        return sqlSettings.getJdbcUser();
    }

    public String getJdbcPassword() {
        return sqlSettings.getJdbcPassword();
    }

    @Override
    public Optional<String> getJdbcDriver() {
        return sqlSettings.getJdbcDriver();
    }
}
