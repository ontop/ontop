package org.protege.osgi.jdbc.prefs;

import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.protege.osgi.jdbc.JdbcRegistry;
import org.protege.osgi.jdbc.RegistryException;

public class Activator implements BundleActivator {
	public static final Logger LOGGER = Logger.getLogger(Activator.class);
	private BundleContext context;
	private static Activator instance;
	private ServiceListener listener = new ServiceListener() {

		public void serviceChanged(ServiceEvent event) {
			if (event.getType() == ServiceEvent.REGISTERED) {
				try {
					installDrivers(event.getServiceReference());
				}
				catch (Exception e) {
					LOGGER.warn("Exception caught installing jdbc drivers.", e);
				}
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		instance = this;
		this.context = context;
		ServiceReference sr = context.getServiceReference(JdbcRegistry.class.getName());
		if (sr != null) {
			installDrivers(sr);
		}
		else {
			String filter = "(objectclass=" + JdbcRegistry.class.getName() + ")";
			context.addServiceListener(listener, filter);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		this.context = null;
	}

	public static Activator getInstance() {
		return instance;
	}
	
	public BundleContext getContext() {
		return context;
	}
	
	private boolean installDrivers(ServiceReference sr) throws MalformedURLException, RegistryException {
		if (sr != null) {
			JdbcRegistry registry = (JdbcRegistry) context.getService(sr);
			try {
				for (DriverInfo driver : PreferencesPanel.getDrivers()) {
					registry.addJdbcDriver(driver.getClassName(), 
							driver.getDriverLocation().toURI().toURL());
				}
				return true;
			}
			finally {
				context.ungetService(sr);
			}
		}
		return false;
	}
	
}
