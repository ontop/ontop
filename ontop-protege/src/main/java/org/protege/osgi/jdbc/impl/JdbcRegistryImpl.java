package org.protege.osgi.jdbc.impl;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.protege.osgi.jdbc.JdbcRegistry;
import org.protege.osgi.jdbc.RegistryException;

public class JdbcRegistryImpl implements JdbcRegistry {
	private List<Driver> drivers = new ArrayList<Driver>();

	public void addJdbcDriver(String className, URL location)
			throws RegistryException {
		try {
			URLClassLoader classLoader = new URLClassLoader(new URL[] { location }, ClassLoader.getSystemClassLoader());
			Class<?> driverClass = classLoader.loadClass(className);
			Driver driver = (Driver) driverClass.newInstance();
			drivers.add(driver);
		}
		catch (InstantiationException ie) {
			throw new RegistryException(ie);
		} catch (ClassNotFoundException e) {
			throw new RegistryException(e);
		} catch (IllegalAccessException e) {
			throw new RegistryException(e);
		}
	}
	
	public void addJdbcDriver(Driver d) {
	    boolean alreadyPresent = false;
	    String driverClassName = d.getClass().getCanonicalName();
	    for (Driver other : drivers) {
	        if (other.getClass().getCanonicalName().equals(driverClassName)) {
	            alreadyPresent = true;
	        }
	    }
	    if (!alreadyPresent) {
	        drivers.add(d);
	    }
	}

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

	public List<Driver> getJdbcDrivers() {
		return Collections.unmodifiableList(drivers);
	}

}
