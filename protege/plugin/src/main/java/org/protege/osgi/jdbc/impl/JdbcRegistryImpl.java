package org.protege.osgi.jdbc.impl;

import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.protege.osgi.jdbc.JdbcRegistry;
import org.protege.osgi.jdbc.JdbcRegistryException;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JdbcRegistryImpl implements JdbcRegistry {

	private static final class DriverWithClassloader {
		final Driver driver;
		final URLClassLoader classLoader;

		DriverWithClassloader(Driver driver, URLClassLoader classLoader) {
			this.driver = driver;
			this.classLoader = classLoader;
		}
	}

	private final List<DriverWithClassloader> drivers = new ArrayList<>();

	@Override
	public void addJdbcDriver(String className, URL location) throws JdbcRegistryException {
		try {
			// TODO: keep track of created class loaders and then close them on exit
			URLClassLoader classLoader = new URLClassLoader(new URL[] { location }, ClassLoader.getSystemClassLoader());
			Class<? extends Driver> driverClass = classLoader.loadClass(className).asSubclass(Driver.class);
			Driver driver = driverClass.getConstructor().newInstance();
			drivers.add(new DriverWithClassloader(driver, classLoader));
		}
		catch (Exception e) {
			throw new JdbcRegistryException(e);
		}
    }

    @Override
    public void removeJdbcDriver(String className) throws JdbcRegistryException {
		Optional<DriverWithClassloader> optionalDriverWithClassloader = drivers.stream()
				.filter(d -> d.driver.getClass().toString().equals(className))
				.findFirst();

		if (optionalDriverWithClassloader.isPresent()) {
			DriverWithClassloader driver = optionalDriverWithClassloader.get();
			try {
				driver.classLoader.close();
			}
			catch (IOException e) {
				throw new JdbcRegistryException(e);
			}
			drivers.remove(driver);
		}
	}

	@Override
	public List<Driver> getJdbcDrivers() {
		return drivers.stream().map(d -> d.driver).collect(ImmutableCollectors.toList());
	}
}
