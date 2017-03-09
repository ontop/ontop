package it.unibz.inf.ontop.model;

import java.net.URI;

/**
 * Methods moved away from OBDADataFactory
 */
public interface MappingFactory {


    OBDADataSource getJDBCDataSource(String jdbcurl, String username,
                                     String password, String driverclass);

    OBDADataSource getJDBCDataSource(String sourceuri, String jdbcurl,
                                     String username, String password, String driverclass);

    OBDADataSource getDataSource(URI id);


    OBDASQLQuery getSQLQuery(String query);
}
