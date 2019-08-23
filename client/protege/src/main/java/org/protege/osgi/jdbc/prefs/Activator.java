package org.protege.osgi.jdbc.prefs;

import org.protege.osgi.jdbc.RegistryException;
import org.osgi.framework.*;
import org.protege.osgi.jdbc.JdbcRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

public class Activator implements BundleActivator {

    private final Logger log = LoggerFactory.getLogger(Activator.class);

	private BundleContext context;
	private static Activator instance;
	private ServiceListener listener = event -> {
        if (event.getType() == ServiceEvent.REGISTERED) {
            try {
                installDrivers(event.getServiceReference());
            }
            catch (Exception e) {
                log.warn("Exception caught installing jdbc drivers.", e);
            }
        }
    };

	@Override
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

    @Override
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
