package it.unibz.inf.ontop.injection;


public interface OntopSystemSQLSettings extends OntopSystemSettings, OntopReformulationSQLSettings,
        OntopSQLCredentialSettings {

    //--------------------------
    // Connection configuration
    //--------------------------

    boolean isKeepAliveEnabled();
    boolean isRemoveAbandonedEnabled();
    int getConnectionTimeout();
    int getConnectionPoolInitialSize();
    int getConnectionPoolMaxSize();

    //--------------------------
    // Keys
    //--------------------------

    String MAX_POOL_SIZE = "jdbc.pool.maxSize";
    String INIT_POOL_SIZE = "jdbc.pool.initialSize";
    String REMOVE_ABANDONED = "jdbc.pool.removeAbandoned";
    // Connection timeout (in ms)
    String CONNECTION_TIMEOUT = "jdbc.pool.connectionTimeout";
    String KEEP_ALIVE = "jdbc.pool.keepAlive";
}
