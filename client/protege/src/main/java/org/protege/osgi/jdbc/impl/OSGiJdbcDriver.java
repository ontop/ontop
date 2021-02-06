package org.protege.osgi.jdbc.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.protege.osgi.jdbc.JdbcRegistry;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class OSGiJdbcDriver implements Driver {
	private final Version version;
	private final JdbcRegistry registry;
	
	public OSGiJdbcDriver(BundleContext context, JdbcRegistry registry) {
		this.registry = registry;
		String versionString = context.getBundle().getHeaders().get(Constants.BUNDLE_VERSION);
		this.version = new Version(versionString);
	}

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		return getDelegate(url) != null;
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		Driver delegate = getDelegate(url);
		return (delegate == null) ? null : delegate.connect(url, info);
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		Driver delegate = getDelegate(url);
		return (delegate == null) ? null : delegate.getPropertyInfo(url, info);
	}

	private Driver getDelegate(String url) throws SQLException {
		for (Driver delegate : registry.getJdbcDrivers())
			if (delegate.acceptsURL(url))
				return delegate;

		return null;
	}

    @Override
	public boolean jdbcCompliant() {
        return registry.getJdbcDrivers().stream().allMatch(Driver::jdbcCompliant);
	}

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getMajorVersion() {
		return version.getMajor();
	}

	@Override
	public int getMinorVersion() {
		return version.getMinor();
	}
}
