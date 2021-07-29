package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import java.util.Properties;


public class OntopSQLCredentialSettingsImpl extends OntopSQLCoreSettingsImpl implements OntopSQLCredentialSettings {

    private final String jdbcUser;
    private final String jdbcPassword;

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

        jdbcUser = getProperty(OntopSQLCredentialSettings.JDBC_USER).orElse("");
        jdbcPassword = getProperty(OntopSQLCredentialSettings.JDBC_PASSWORD).orElse("");
    }

    @Override
    public String getJdbcUser() {
        return jdbcUser;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }
}
