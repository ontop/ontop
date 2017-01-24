package it.unibz.inf.ontop.injection;

public interface OntopSQLSettings extends OntopOBDASettings {

    String getJdbcUrl();
    String getJdbcName();
    String getJdbcUser();
    String getJdbcPassword();
    String getJdbcDriver();

    //-------
    // Keys
    //-------

    String JDBC_URL = "jdbc.url";
    String JDBC_NAME = "jdbc.name";
    String JDBC_USER = "jdbc.user";
    String JDBC_PASSWORD = "jdbc.password";
    String JDBC_DRIVER = "jdbc.driver";
}
