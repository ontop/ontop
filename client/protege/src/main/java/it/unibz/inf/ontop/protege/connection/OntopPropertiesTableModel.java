package it.unibz.inf.ontop.protege.connection;


import com.google.common.collect.ImmutableSet;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class OntopPropertiesTableModel extends AbstractTableModel {

    private final List<String> keys = new ArrayList<>();

    private DataSource datasource;

    public void clear(DataSource datasource) {
        this.datasource = datasource;
        keys.clear();
        keys.addAll(datasource.getPropertyKeys());
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() { return keys.size(); }

    @Override
    public int getColumnCount() { return 2; }

    @Override
    public String getColumnName(int column) { return column == 0 ? "Property" : "Value"; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String key = keys.get(rowIndex);
        return columnIndex == 0 ? key : datasource.getProperty(key);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return String.class;
    }

    public void setProperty(String name, String value) {
        if (datasource.getProperty(name) == null) {
            keys.add(name);
            datasource.setProperty(name, value);
            fireTableRowsInserted(keys.size(), keys.size());
        }
        else {
            int rowIndex = keys.indexOf(name);
            datasource.setProperty(name, value);
            fireTableCellUpdated(rowIndex, 1);
        }
    }

    void removeRow(int rowIndex) {
        if (rowIndex < keys.size()) {
            String key = keys.remove(rowIndex);
            datasource.removeProperty(key);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    String getName(int row) {
        return (String)getValueAt(row, 0);
    }

    String getValue(int row) {
        return (String)getValueAt(row, 1);
    }

    ImmutableSet<String> getNames() {
        return datasource.getPropertyKeys();
    }

    public boolean canBeInRow(Object key, int row) {
        for (int i = 0; i < keys.size(); i++)
            if (i != row)
                if (keys.get(i).equals(key))
                    return false;
        return true;
    }

}
