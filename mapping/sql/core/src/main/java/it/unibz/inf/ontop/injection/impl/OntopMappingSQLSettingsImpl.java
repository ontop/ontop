package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;

import java.util.Optional;
import java.util.Properties;

import static it.unibz.inf.ontop.injection.impl.OntopSQLCoreSettingsImpl.loadSQLCoreProperties;


public class OntopMappingSQLSettingsImpl extends OntopMappingSettingsImpl implements OntopMappingSQLSettings {

    private static final String DEFAULT_PROPERTY_FILE = "mapping-sql-default.properties";
    private final OntopSQLCredentialSettings sqlSettings;

    OntopMappingSQLSettingsImpl(Properties properties) {
        super(loadProperties(properties));
        sqlSettings = new OntopSQLCredentialSettingsImpl(copyProperties());
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = loadSQLCoreProperties(userProperties);
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
    public Optional<String> getJdbcUser() {
        return sqlSettings.getJdbcUser();
    }

    @Override
    public Optional<String> getJdbcPassword() {
        return sqlSettings.getJdbcPassword();
    }

    @Override
    public String getJdbcDriver() {
        return sqlSettings.getJdbcDriver();
    }
}
