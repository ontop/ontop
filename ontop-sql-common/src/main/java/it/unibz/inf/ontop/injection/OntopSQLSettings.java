package it.unibz.inf.ontop.injection;

public interface OntopSQLSettings extends OntopOBDASettings {

    String getJdbcUrl();
    String getDBName();
    String getDBUser();
    String getDbPassword();
    String getJdbcDriver();

    //-------
    // Keys
    //-------

    String JDBC_URL = "JDBC_URL";
    String DB_NAME = "DB_NAME";
    String DB_USER = "DB_USER";
    String DB_PASSWORD = "DB_PASSWORD";
    String JDBC_DRIVER = "JDBC_DRIVER";
}
