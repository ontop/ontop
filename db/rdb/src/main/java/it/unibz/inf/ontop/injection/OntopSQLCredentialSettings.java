package it.unibz.inf.ontop.injection;

import java.util.Optional;
import java.util.Properties;

public interface OntopSQLCredentialSettings extends OntopSQLCoreSettings {

    Optional<String> getJdbcUser();
    Optional<String> getJdbcPassword();

    Properties getAdditionalJDBCProperties();

    //-------
    // Keys
    //-------

    String JDBC_USER = "jdbc.user";
    String JDBC_PASSWORD = "jdbc.password";
    String ADDITIONAL_JDBC_PROPERTY_PREFIX = "jdbc.property.";
}
