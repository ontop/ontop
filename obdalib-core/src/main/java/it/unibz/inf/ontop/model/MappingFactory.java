package it.unibz.inf.ontop.model;

import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDASQLQuery;
import it.unibz.inf.ontop.utils.JdbcTypeMapper;

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

    JdbcTypeMapper getJdbcTypeMapper();


    OBDASQLQuery getSQLQuery(String query);
}
