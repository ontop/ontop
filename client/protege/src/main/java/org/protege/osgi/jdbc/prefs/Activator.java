package org.protege.osgi.jdbc.prefs;

import org.osgi.framework.*;
import org.protege.osgi.jdbc.JdbcRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private final Logger log = LoggerFactory.getLogger(Activator.class);

	private BundleContext context;
	private static Activator instance;

	@Override
	public void start(BundleContext context) throws Exception {
		instance = this;
		this.context = context;
		ServiceReference<JdbcRegistry> sr = context.getServiceReference(JdbcRegistry.class);
		if (sr != null) {
			installDrivers(sr);
		}
		else {
			context.addServiceListener(evt -> {
				if (evt.getType() == ServiceEvent.REGISTERED) {
					ServiceReference<JdbcRegistry> sr1 = (ServiceReference<JdbcRegistry>)evt.getServiceReference();
					installDrivers(sr1);
				}
			}, "(objectclass=" + JdbcRegistry.class.getName() + ")");
		}
	}

    @Override
	public void stop(BundleContext context) {
		this.context = null;
	}

	public static BundleContext getContext() {
		return instance.context;
	}
	
	private void installDrivers(ServiceReference<JdbcRegistry> sr) {
		if (sr != null) {
			JdbcRegistry registry = context.getService(sr);
			try {
				for (JDBCDriverInfo driver : JDBCDriverTableModel.getDrivers()) {
					try {
						registry.addJdbcDriver(driver.getClassName(), driver.getDriverURL());
					}
					catch (Exception e) {
						log.warn("Exception caught installing JDBC driver: ", e);
					}
				}
			}
			finally {
				context.ungetService(sr);
			}
		}
	}
}
