package org.protege.osgi.jdbc.impl;

import org.protege.osgi.jdbc.JdbcRegistry;
import org.protege.osgi.jdbc.JdbcRegistryException;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JdbcRegistryImpl implements JdbcRegistry {

	private final List<Driver> drivers = new ArrayList<>();

	@Override
	public void addJdbcDriver(String className, URL location) throws JdbcRegistryException {
		try {
			URLClassLoader classLoader = new URLClassLoader(new URL[] { location }, ClassLoader.getSystemClassLoader());
			Class<?> driverClass = classLoader.loadClass(className);
			Driver driver = (Driver) driverClass.newInstance();
			drivers.add(driver);
		}
		catch (InstantiationException | ClassNotFoundException | IllegalAccessException ie) {
			throw new JdbcRegistryException(ie);
		}
    }

    @Override
    public void removeJdbcDriver(String className) {
		Driver found = null;
		for (Driver driver : drivers) {
			if (driver.getClass().toString().equals(className)) {
				found = driver;
				break;
			}
		}
		if (found != null) {
			drivers.remove(found);
		}
	}

	@Override
	public List<Driver> getJdbcDrivers() {
		return Collections.unmodifiableList(drivers);
	}
}
