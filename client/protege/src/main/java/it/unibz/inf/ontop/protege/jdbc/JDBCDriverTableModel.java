package it.unibz.inf.ontop.protege.jdbc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.osgi.util.tracker.ServiceTracker;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.osgi.jdbc.JdbcRegistry;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.sql.Driver;
import java.util.*;
import java.util.function.Function;

public class JDBCDriverTableModel extends AbstractTableModel {
    private static final long serialVersionUID = -7588371899390500462L;

	private final List<JDBCDriverInfo> drivers;
    private final Set<String> installedDriverClasses = new HashSet<>();
    private final ServiceTracker<?,?> jdbcRegistryTracker;

	private final ImmutableList<Map.Entry<String, Function<JDBCDriverInfo, String>>> COLUMNS = ImmutableList.of(
			Maps.immutableEntry("Description", JDBCDriverInfo::getDescription),
			Maps.immutableEntry("Class name", JDBCDriverInfo::getClassName),
			Maps.immutableEntry("Location", JDBCDriverInfo::getDriverPath),
			Maps.immutableEntry("Status", info -> installedDriverClasses.contains(info.getClassName()) ? "ready" : "down"));

	public JDBCDriverTableModel(ServiceTracker<?,?> jdbcRegistryTracker) {
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
	public Object getValueAt(int rowIndex, int columnIndex) {
		JDBCDriverInfo info = drivers.get(rowIndex);
		return COLUMNS.get(columnIndex).getValue().apply(info);
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

	public JDBCDriverInfo getDriver(int row) {
		return drivers.get(row);
	}

	public void addDriver(JDBCDriverInfo driver) {
		JDBCDriverInfo toRemove = null;
		for (JDBCDriverInfo existingDriver : drivers) {
			if (existingDriver.getClassName().equals(driver.getClassName())) {
				toRemove = existingDriver;
				break;
			}
		}
		if (toRemove != null) {
		    drivers.remove(toRemove);
		}
		drivers.add(driver);
		updateDriverStatus();
	}
	
	public void removeDrivers(int row) {
		drivers.remove(row);
		updateDriverStatus();
	}
	
	public void replaceDriver(int row, JDBCDriverInfo newDriver) {
	    drivers.remove(row);
	    drivers.add(row, newDriver);
		updateDriverStatus();
	}


	public void storeDriverInfoInPreferences() {
		Preferences prefs = PreferencesManager.getInstance().getPreferencesForSet(JDBCPreferencesPanel.PREFERENCES_SET, JDBCPreferencesPanel.DRIVER_PREFERENCES_KEY);
		List<String> prefsStringList = new ArrayList<>();
		for (JDBCDriverInfo driver : drivers) {
			prefsStringList.add(driver.getDescription());
			prefsStringList.add(driver.getClassName());
			prefsStringList.add(driver.getDriverPath());
		}
		prefs.clear();
		prefs.putStringList(JDBCPreferencesPanel.DRIVER_PREFERENCES_KEY, prefsStringList);
	}

	public static List<JDBCDriverInfo> getDriverInfoFromPreferences() {
		List<JDBCDriverInfo> drivers = new ArrayList<>();
		Preferences prefs = PreferencesManager.getInstance().getPreferencesForSet(JDBCPreferencesPanel.PREFERENCES_SET, JDBCPreferencesPanel.DRIVER_PREFERENCES_KEY);
		Iterator<String> driverStrings = prefs.getStringList(JDBCPreferencesPanel.DRIVER_PREFERENCES_KEY, new ArrayList<>()).iterator();
		while (driverStrings.hasNext()) {
			drivers.add(new JDBCDriverInfo(driverStrings.next(), driverStrings.next(), new File(driverStrings.next())));
		}
		return drivers;
	}

}
