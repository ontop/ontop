package org.semanticweb.ontop.nativeql;

/**
 * Wrapper for direct connections to DBs accepting various native query languages.
 */
public interface DBConnectionWrapper {

    /**
     * Implementations of this interface should returns
     * a object specific to their DB (e.g. java.sql.Connection for JDBC connections).
     */
    Object getConnection();
}
