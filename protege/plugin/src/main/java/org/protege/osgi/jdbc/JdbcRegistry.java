package org.protege.osgi.jdbc;

import java.net.URL;
import java.sql.Driver;
import java.util.Collection;

public interface JdbcRegistry {
	void addJdbcDriver(String className, URL location) throws JdbcRegistryException;

    void removeJdbcDriver(String className) throws JdbcRegistryException;

    Collection<Driver> getJdbcDrivers();
}
