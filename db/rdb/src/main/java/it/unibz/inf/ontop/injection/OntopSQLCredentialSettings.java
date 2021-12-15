package it.unibz.inf.ontop.injection;

import java.util.Optional;

public interface OntopSQLCredentialSettings extends OntopSQLCoreSettings {

    Optional<String> getJdbcUser();
    Optional<String> getJdbcPassword();

    //-------
    // Keys
    //-------

    String JDBC_USER = "jdbc.user";
    String JDBC_PASSWORD = "jdbc.password";
}
