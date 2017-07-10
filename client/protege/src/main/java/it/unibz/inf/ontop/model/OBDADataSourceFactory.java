package it.unibz.inf.ontop.model;

public interface OBDADataSourceFactory {

    OBDADataSource getJDBCDataSource(String jdbcurl, String username,
                                     String password, String driverclass);

    OBDADataSource getJDBCDataSource(String sourceuri, String jdbcurl,
                                     String username, String password, String driverclass);
}
