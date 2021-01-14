package org.protege.osgi.jdbc.prefs;

import org.osgi.util.tracker.ServiceTracker;
import org.protege.osgi.jdbc.JdbcRegistry;

import javax.swing.table.AbstractTableModel;
import java.sql.Driver;
import java.util.*;

public class DriverTableModel extends AbstractTableModel {
    private static final long serialVersionUID = -7588371899390500462L;
    
    private final List<DriverInfo> drivers;
    private final Set<String> foundDrivers = new HashSet<>();
    private ServiceTracker jdbcRegistryTracker;
    
	public enum Column {
		DESCRIPTION("Description"), STATUS("Status");
		
		private final String name;
		
		Column(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	

	
	public DriverTableModel(List<DriverInfo> drivers, ServiceTracker jdbcRegistryTracker) {
		this.drivers  = drivers;
		this.jdbcRegistryTracker = jdbcRegistryTracker;
		refresh();
	}

	public int getColumnCount() {
		return Column.values().length;
	}
	
	@Override
	public String getColumnName(int column) {
	    Column col = Column.values()[column];
	    return col.getName();
	}

	public int getRowCount() {
		return drivers.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		DriverInfo info = drivers.get(rowIndex);
		switch (Column.values()[columnIndex]) {
		case DESCRIPTION:
			return info != null ? info.getDescription() : null;
		case STATUS:
			return foundDrivers.contains(info.getClassName()) ? "ready" : "down";
	    default:
	    	throw new UnsupportedOperationException("Programmer error");
		}
	}
	
	public void refresh() {
		foundDrivers.clear();
		try {
		    jdbcRegistryTracker.open();
		    for (Object o : jdbcRegistryTracker.getServices()) {
		        JdbcRegistry registry = (JdbcRegistry) o;
		        for (Driver d : registry.getJdbcDrivers()) {
		            foundDrivers.add(d.getClass().getName());
		        }
		    }
		}
		finally {
		    jdbcRegistryTracker.close();
		}
		fireTableStructureChanged();
	}
	
	public List<DriverInfo> getDrivers() {
	    return drivers;
	}
	
	public void addDriver(DriverInfo driver) {
		DriverInfo toRemove = null;
		for (DriverInfo existingDriver : drivers) {
			if (existingDriver.getClassName().equals(driver.getClassName())) {
				toRemove = existingDriver;
				break;
			}
		}
		if (toRemove != null) {
		    drivers.remove(toRemove);
		}
		drivers.add(driver);
		refresh();
	}
	
	public void removeDrivers(List<Integer> rows) {
	    Collections.sort(rows);
	    Collections.reverse(rows);
	    for (Integer row : rows) {
	        drivers.remove(row.intValue());
	    }
		refresh();
	}
	
	public void replaceDriver(int row, DriverInfo newDriver) {
	    drivers.remove(row);
	    drivers.add(row, newDriver);
	}


}
