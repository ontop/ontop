package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopReformulationSQLSettings;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;

import java.util.Properties;

public class OntopReformulationSQLSettingsImpl extends OntopReformulationSettingsImpl
        implements OntopReformulationSQLSettings {

    private static final String DEFAULT_FILE = "reformulation-sql-default.properties";

    private final OntopSQLCoreSettings sqlSettings;

    OntopReformulationSQLSettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
        sqlSettings = new OntopSQLCoreSettingsImpl(copyProperties());
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = loadDefaultQASQLProperties();
        properties.putAll(userProperties);
        return OntopSQLCoreSettingsImpl.loadSQLCoreProperties(properties);
    }

    static Properties loadDefaultQASQLProperties() {
        return loadDefaultPropertiesFromFile(OntopReformulationSQLSettings.class, DEFAULT_FILE);
    }

    @Override
    public String getJdbcUrl() {
        return sqlSettings.getJdbcUrl();
    }

    @Override
    public String getJdbcDriver() {
        return sqlSettings.getJdbcDriver();
    }
}
