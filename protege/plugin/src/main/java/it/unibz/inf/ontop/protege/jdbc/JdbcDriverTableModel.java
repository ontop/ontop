package it.unibz.inf.ontop.protege.jdbc;

import it.unibz.inf.ontop.shaded.com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.shaded.com.google.common.collect.Maps;
import org.osgi.util.tracker.ServiceTracker;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.osgi.jdbc.JdbcRegistry;
import org.protege.osgi.jdbc.JdbcRegistryException;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.sql.Driver;
import java.util.*;
import java.util.function.Function;

public class JdbcDriverTableModel extends AbstractTableModel {

	// drivers are uniquely identified by their class names
	private final List<JdbcDriverInfo> drivers;
    private final Set<String> installedDriverClasses = new HashSet<>();
    private final ServiceTracker<?,?> jdbcRegistryTracker;

	private final ImmutableList<Map.Entry<String, Function<JdbcDriverInfo, String>>> COLUMNS = ImmutableList.of(
			Maps.immutableEntry("Description", JdbcDriverInfo::getDescription),
			Maps.immutableEntry("Class name", JdbcDriverInfo::getClassName),
			Maps.immutableEntry("Location", JdbcDriverInfo::getDriverPath),
			Maps.immutableEntry("Status", info -> installedDriverClasses.contains(info.getClassName()) ? "ready" : "down"));

	public JdbcDriverTableModel(ServiceTracker<?,?> jdbcRegistryTracker) {
		this.jdbcRegistryTracker = jdbcRegistryTracker;
		this.drivers = getDriverInfoFromPreferences();
		updateDriverStatus();
	}

	@Override
	public int getColumnCount() {
		return COLUMNS.size();
	}
	
	@Override
	public String getColumnName(int column) {
	    return COLUMNS.get(column).getKey();
	}

	@Override
	public int getRowCount() {
		return drivers.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		JdbcDriverInfo info = getDriver(row);
		return COLUMNS.get(column).getValue().apply(info);
	}

	private void updateDriverStatus() {
		installedDriverClasses.clear();
		try {
		    jdbcRegistryTracker.open();
		    for (Object o : jdbcRegistryTracker.getServices()) {
		        JdbcRegistry registry = (JdbcRegistry) o;
		        for (Driver d : registry.getJdbcDrivers()) {
		            installedDriverClasses.add(d.getClass().getName());
		        }
		    }
		}
		finally {
		    jdbcRegistryTracker.close();
		}
		fireTableDataChanged();
	}

	public JdbcDriverInfo getDriver(int row) {
		return drivers.get(row);
	}

	public void addOrReplaceDriver(JdbcDriverInfo driver) throws JdbcRegistryException {
		Optional<JdbcDriverInfo> toRemove = drivers.stream()
				.filter(d -> d.getClassName().equals(driver.getClassName()))
				.findFirst();

		if (toRemove.isPresent())
			removeAndUnloadDriver(toRemove.get());

		drivers.add(driver);
		updateDriverStatus();
	}
	
	public void removeDriver(int row) throws JdbcRegistryException {
		removeAndUnloadDriver(getDriver(row));
		updateDriverStatus();
	}
	
	public void replaceDriver(int row, JdbcDriverInfo newDriver) throws JdbcRegistryException {
	    removeAndUnloadDriver(getDriver(row));
	    drivers.add(row, newDriver);
		updateDriverStatus();
	}

	private void removeAndUnloadDriver(JdbcDriverInfo driver) throws JdbcRegistryException {
		try {
			jdbcRegistryTracker.open();
			for (Object o : jdbcRegistryTracker.getServices()) {
				JdbcRegistry registry = (JdbcRegistry) o;
				registry.removeJdbcDriver(driver.getClassName());
			}
		}
		finally {
			jdbcRegistryTracker.close();
		}
		drivers.remove(driver);
	}

	public void storeDriverInfoInPreferences() {
		Preferences prefs = PreferencesManager.getInstance().getPreferencesForSet(JdbcPreferencesPanel.PREFERENCES_SET, JdbcPreferencesPanel.DRIVER_PREFERENCES_KEY);
		List<String> prefsStringList = new ArrayList<>();
		for (JdbcDriverInfo driver : drivers) {
			prefsStringList.add(driver.getDescription());
			prefsStringList.add(driver.getClassName());
			prefsStringList.add(driver.getDriverPath());
		}
		prefs.clear();
		prefs.putStringList(JdbcPreferencesPanel.DRIVER_PREFERENCES_KEY, prefsStringList);
	}

	public static List<JdbcDriverInfo> getDriverInfoFromPreferences() {
		List<JdbcDriverInfo> drivers = new ArrayList<>();
		Preferences prefs = PreferencesManager.getInstance().getPreferencesForSet(JdbcPreferencesPanel.PREFERENCES_SET, JdbcPreferencesPanel.DRIVER_PREFERENCES_KEY);
		Iterator<String> driverStrings = prefs.getStringList(JdbcPreferencesPanel.DRIVER_PREFERENCES_KEY, new ArrayList<>()).iterator();
		while (driverStrings.hasNext()) {
			drivers.add(new JdbcDriverInfo(driverStrings.next(), driverStrings.next(), new File(driverStrings.next())));
		}
		return drivers;
	}

}
