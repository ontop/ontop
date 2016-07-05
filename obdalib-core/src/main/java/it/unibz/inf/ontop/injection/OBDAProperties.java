package it.unibz.inf.ontop.injection;

import java.util.Optional;

/**
 * General properties.
 *
 * Focuses on implementation class declaration
 * for the core module of Ontop.
 *
 * Validation is not done at construction time but on demand.
 *
 * Immutable!
 *
 * TODO: update this description
 *
 */
public interface OBDAProperties {

    //-------------------
    // High-level methods
    //-------------------

    Optional<String> getMappingFilePath();

    boolean obtainFullMetadata();

    Optional<String> getJdbcUrl();
    // TODO: continue


    //-------
    // Keys
    //-------

    String JDBC_URL = "JDBC_URL";
    String DB_NAME = "DB_NAME";
    String DB_USER = "DBUSER";
    String DB_PASSWORD = "DBPASSWORD";
    String JDBC_DRIVER = "JDBC_DRIVER";

    String OBTAIN_FULL_METADATA = "OBTAIN_FULL_METADATA";

    // String DB_CONSTRAINTS = "DB_CONSTRAINTS";

    String MAPPING_FILE_PATH = "MAPPING_FILE_PATH";


    //-------------------
    // Low-level methods
    //-------------------

    boolean getBoolean(String key);

    int getInteger(String key);

    String getProperty(String key);

    boolean contains(Object key);

}
