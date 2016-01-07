package org.protege.osgi.jdbc.impl;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.protege.osgi.jdbc.JdbcRegistry;
import org.protege.osgi.jdbc.OSGiJdbcDriver;

public class Activator implements BundleActivator {
	private JdbcRegistry registry;
	private OSGiJdbcDriver driver;
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws SQLException {
		try {
			registry = new JdbcRegistryImpl();
			context.registerService(JdbcRegistry.class.getName(), registry, new Hashtable<String, String>());
			driver = new OSGiJdbcDriver(context, registry);
			DriverManager.registerDriver(driver);
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		DriverManager.deregisterDriver(driver);
		driver = null;
	}


}
