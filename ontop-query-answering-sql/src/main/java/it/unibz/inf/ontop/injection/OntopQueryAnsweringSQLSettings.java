package it.unibz.inf.ontop.injection;


public interface OntopQueryAnsweringSQLSettings extends OntopQueryAnsweringSettings, OntopSQLCoreSettings {

    //--------------------------
    // Connection configuration
    //--------------------------

    boolean isKeepAliveEnabled();
    boolean isRemoveAbandonedEnabled();
    int getAbandonedTimeout();
    int getConnectionPoolInitialSize();
    int getConnectionPoolMaxSize();

    //--------------------------
    // Keys
    //--------------------------

    String MAX_POOL_SIZE = "jdbc.pool.maxSize";
    String INIT_POOL_SIZE = "jdbc.pool.initialSize";
    String REMOVE_ABANDONED = "jdbc.pool.removeAbandoned";
    String ABANDONED_TIMEOUT = "jdbc.pool.abandonedTimeout";
    String KEEP_ALIVE = "jdbc.pool.keepAlive";
}
