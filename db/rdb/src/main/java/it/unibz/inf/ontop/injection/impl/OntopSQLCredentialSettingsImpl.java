package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;

import java.util.Optional;
import java.util.Properties;


public class OntopSQLCredentialSettingsImpl extends OntopSQLCoreSettingsImpl implements OntopSQLCredentialSettings {

    private final Optional<String> jdbcUser;
    private final Optional<String> jdbcPassword;

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
    }

    @Override
    public Optional<String> getJdbcUser() {
        return jdbcUser;
    }

    @Override
    public Optional<String> getJdbcPassword() {
        return jdbcPassword;
    }
}
