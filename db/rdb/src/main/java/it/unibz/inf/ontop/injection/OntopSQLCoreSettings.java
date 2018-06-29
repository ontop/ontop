package it.unibz.inf.ontop.injection;

public interface OntopSQLCoreSettings extends OntopOBDASettings {

    String getJdbcUrl();
    String getJdbcName();
    String getJdbcDriver();

    //-------
    // Keys
    //-------

    String JDBC_URL = "jdbc.url";
    String JDBC_NAME = "jdbc.name";
    String JDBC_DRIVER = "jdbc.driver";
}
