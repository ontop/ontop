package it.unibz.inf.ontop.injection.impl;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.Properties;


public class OntopSQLCredentialSettingsImpl extends OntopSQLCoreSettingsImpl implements OntopSQLCredentialSettings {

    private final Optional<String> jdbcUser;
    private final Optional<String> jdbcPassword;
    private final ImmutableMap<Object, Object> additionalJDBCPropertyMap;

    /**
     * Beware: immutable class!
     * <p>
     * Recommended constructor.
     * <p>
     * Changing the Properties object afterwards will not have any effect
     * on this OntopSQLCredentialSettings object.
     *
     * @param userProperties
     */
    protected OntopSQLCredentialSettingsImpl(Properties userProperties) {
        super(userProperties);

        jdbcUser = getProperty(OntopSQLCredentialSettings.JDBC_USER);
        jdbcPassword = getProperty(OntopSQLCredentialSettings.JDBC_PASSWORD);

        additionalJDBCPropertyMap = extractAdditionalJDBCPropertyMap(userProperties);
    }

    @Override
    public Optional<String> getJdbcUser() {
        return jdbcUser;
    }

    @Override
    public Optional<String> getJdbcPassword() {
        return jdbcPassword;
    }

    @Override
    public Properties getAdditionalJDBCProperties() {
        Properties properties = new Properties();
        properties.putAll(additionalJDBCPropertyMap);
        return properties;
    }

    private static ImmutableMap<Object, Object> extractAdditionalJDBCPropertyMap(Properties properties) {
        return properties.keySet().stream()
                .filter(k -> k instanceof String)
                .map(k -> (String) k)
                .filter(k -> k.startsWith(ADDITIONAL_JDBC_PROPERTY_PREFIX))
                .filter(k -> k.length() > ADDITIONAL_JDBC_PROPERTY_PREFIX.length())
                .collect(ImmutableCollectors.toMap(
                        k -> k.substring(ADDITIONAL_JDBC_PROPERTY_PREFIX.length()),
                        properties::get));
    }
}
