package org.protege.osgi.jdbc.preferences;

import org.osgi.framework.*;
import org.protege.osgi.jdbc.JdbcRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCPreferencesPanelBundleActivator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCPreferencesPanelBundleActivator.class);

	private static BundleContext context;

	@Override
	public void start(BundleContext context) throws Exception {
		JDBCPreferencesPanelBundleActivator.context = context;

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
		JDBCPreferencesPanelBundleActivator.context = null;
	}

	public static BundleContext getContext() {
		return context;
	}
	
	private static void installDrivers(ServiceReference<JdbcRegistry> sr) {
		if (sr != null) {
			JdbcRegistry registry = context.getService(sr);
			try {
				for (JDBCDriverInfo driver : JDBCDriverTableModel.getDrivers()) {
					try {
						registry.addJdbcDriver(driver.getClassName(), driver.getDriverURL());
					}
					catch (Exception e) {
						LOGGER.warn("Exception caught installing JDBC driver: ", e);
					}
				}
			}
			finally {
				context.ungetService(sr);
			}
		}
	}
}
