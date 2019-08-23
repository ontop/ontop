package it.unibz.inf.ontop.injection;

public interface OntopSQLCredentialSettings extends OntopSQLCoreSettings {

    String getJdbcUser();
    String getJdbcPassword();

    //-------
    // Keys
    //-------

    String JDBC_USER = "jdbc.user";
    String JDBC_PASSWORD = "jdbc.password";
}
