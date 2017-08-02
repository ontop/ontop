package org.protege.osgi.jdbc.prefs;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;
import org.protege.osgi.jdbc.JdbcRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PreferencesPanel extends OWLPreferencesPanel {
    private static final long serialVersionUID = 2892884854196959326L;

    public static final String PREFERENCES_SET="org.protege.osgi.jdbc.prefs";
    public static final String DRIVER_PREFERENCES_KEY="driver.list";
    public static final String DEFAULT_DRIVER_DIR="driver.dir";
    
    private JTable table;
    private DriverTableModel driverTableModel;
    private ServiceTracker jdbcRegistryTracker;

    @Override
    public void initialise() throws Exception {
        BundleContext context = Activator.getInstance().getContext();
        jdbcRegistryTracker = new ServiceTracker(context, JdbcRegistry.class.getName(), null);
        setPreferredSize(new java.awt.Dimension(620, 300));
        setLayout(new GridBagLayout());
        GridBagConstraints listConstraints = new GridBagConstraints();
        listConstraints.fill = GridBagConstraints.BOTH;
        listConstraints.gridx = 0;
        listConstraints.gridy = 0;
        listConstraints.gridwidth = 3;
        listConstraints.gridheight = 3;
        listConstraints.weightx=1;
        listConstraints.weighty=1;
        GridBagConstraints buttonsConstraints = new GridBagConstraints();
        buttonsConstraints.gridx = 0;
        buttonsConstraints.gridy = 4;
        add(createList(), listConstraints);
        add(createButtons(), buttonsConstraints);
    }

    @Override
    public void applyChanges() {
        setDrivers(driverTableModel.getDrivers());
    }

    @Override
    public void dispose()  { }


    public static List<DriverInfo> getDrivers() {
    	List<DriverInfo> drivers  = new ArrayList<>();
    	Preferences prefs = PreferencesManager.getInstance().getPreferencesForSet(PREFERENCES_SET, DRIVER_PREFERENCES_KEY);
    	Iterator<String> driverStrings  = prefs.getStringList(DRIVER_PREFERENCES_KEY, new ArrayList<>()).iterator();
    	while (driverStrings.hasNext()) {
    		drivers.add(new DriverInfo(driverStrings.next(), driverStrings.next(), new File(driverStrings.next())));
    	}
    	return drivers;
    }
    
    private void setDrivers(List<DriverInfo> drivers) {
    	Preferences prefs = PreferencesManager.getInstance().getPreferencesForSet(PREFERENCES_SET, DRIVER_PREFERENCES_KEY);
    	List<String>  prefsStringList = new ArrayList<>();
    	for  (DriverInfo driver : drivers) {
    		prefsStringList.add(driver.getDescription());
    		prefsStringList.add(driver.getClassName());
    		prefsStringList.add(driver.getDriverLocation().getPath());
    	}
        prefs.clear();
    	prefs.putStringList(DRIVER_PREFERENCES_KEY, prefsStringList);
    }
    

    private JComponent createList() {
    	driverTableModel = new DriverTableModel(getDrivers(), jdbcRegistryTracker);
        table = new JTable(driverTableModel);
        return new JScrollPane(table);
    }
    
    private JComponent createButtons() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton add = new JButton("Add");
        add.addActionListener(this::handleAdd);
        panel.add(add);
        JButton remove = new JButton("Remove");
        panel.add(remove);
        remove.addActionListener(this::handleRemove);
        JButton edit = new JButton("Edit");
        panel.add(edit);
        edit.addActionListener(this::handleEdit);
        return panel;
    }

    public void handleAdd(ActionEvent e) {
        final EditorPanel editor = new EditorPanel(jdbcRegistryTracker);
        DriverInfo info = editor.askUserForDriverInfo();
        if (info != null) {
            driverTableModel.addDriver(info);
        }
    }

    public void handleRemove(ActionEvent e) {
        int[] rows = table.getSelectedRows();
        List<Integer> rowList = new ArrayList<>();
        for (int row : rows) {
            rowList.add(row);
        }
        driverTableModel.removeDrivers(rowList);
    }

    public void handleEdit(ActionEvent e) {
        int row = table.getSelectedRow();
        DriverInfo existing = driverTableModel.getDrivers().get(row);
        EditorPanel editor = new EditorPanel(jdbcRegistryTracker,
                existing.getDescription(),
                existing.getClassName(),
                existing.getDriverLocation());
        DriverInfo info = editor.askUserForDriverInfo();
        driverTableModel.replaceDriver(row, info);
    }

}
