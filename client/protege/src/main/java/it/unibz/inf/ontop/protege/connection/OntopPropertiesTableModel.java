package it.unibz.inf.ontop.protege.connection;

import com.google.common.collect.ImmutableSet;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class OntopPropertiesTableModel extends AbstractTableModel {

    public static final String NEW_KEY = "<new key>";
    public static final String NEW_VALUE = "<new value>";

    private final List<String> keys = new ArrayList<>();

    private DataSource datasource;
    private String newlyAddedValue = "";

    public void clear(DataSource datasource) {
        this.datasource = datasource;

        keys.clear();
        Properties properties = datasource.asProperties();
        ImmutableSet<String> connectionParameterNames = DataSource.getConnectionParameterNames();
        for (Map.Entry<Object, Object> p : properties.entrySet())
            if (!connectionParameterNames.contains(p.getKey())) {
                keys.add(p.getKey().toString());
            }
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() { return keys.size() + 1; }

    @Override
    public int getColumnCount() { return 2; }

    @Override
    public String getColumnName(int column) { return column == 0 ? "Property" : "Value"; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex == keys.size())
            return columnIndex == 0 ? NEW_KEY : newlyAddedValue.isEmpty() ? NEW_VALUE : newlyAddedValue;

        String key = keys.get(rowIndex);
        return columnIndex == 0 ? key : datasource.asProperties().get(key);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return String.class;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            String key = (String)aValue;
            if (rowIndex == keys.size()) {
                if (NEW_KEY.equals(key))
                    return;

                datasource.setProperty(key, newlyAddedValue);
                newlyAddedValue = "";
                keys.add(rowIndex, key);
                fireTableRowsInserted(rowIndex + 1, rowIndex + 1);
            }
            else {
                String oldKey = keys.set(rowIndex, key);
                datasource.renameProperty(oldKey, key);
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
        else {
            String value = (String) aValue;
            if (rowIndex == keys.size()) {
                newlyAddedValue = value;
            }
            else {
                String key = keys.get(rowIndex);
                datasource.setProperty(key, value);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    void removeRow(int rowIndex) {
        if (rowIndex < keys.size()) {
            String key = keys.remove(rowIndex);
            datasource.removeProperty(key);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public boolean canBeInRow(Object key, int row) {
        for (int i = 0; i < keys.size(); i++)
            if (i != row)
                if (keys.get(i).equals(key))
                    return false;
        return true;
    }

}
