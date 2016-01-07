package org.protege.osgi.jdbc.prefs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;
import org.protege.osgi.jdbc.JdbcRegistry;

public class PreferencesPanel extends OWLPreferencesPanel {
    private static final long serialVersionUID = 2892884854196959326L;
    private Logger log = Logger.getLogger(PreferencesPanel.class);
    
    public static final String PREFERENCES_SET="org.protege.osgi.jdbc.prefs";
    public static final String DRIVER_PREFERENCES_KEY="driver.list";
    public static final String DEFAULT_DRIVER_DIR="driver.dir";
    
    private JTable table;
    private DriverTableModel driverTableModel;
    private JButton add;
    private JButton remove;
    private JButton edit;
    private ServiceTracker jdbcRegistryTracker;


    /* *******************************************************
     * OWLPreferencesPanel methods
     */
    public void initialise() throws Exception {
        BundleContext context = Activator.getInstance().getContext();
        jdbcRegistryTracker = new ServiceTracker(context, JdbcRegistry.class.getName(), null);
        setLayout(new BorderLayout());
        add(createList(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);
    }



    @Override
    public void applyChanges() {
        setDrivers(driverTableModel.getDrivers());
    }
    
    public void dispose() throws Exception {

    }
    
    /* *******************************************************
     * Preferences
     */
    
    public static List<DriverInfo> getDrivers() {
    	List<DriverInfo> drivers  = new ArrayList<DriverInfo>();
    	Preferences prefs = PreferencesManager.getInstance().getPreferencesForSet(PREFERENCES_SET, DRIVER_PREFERENCES_KEY);
    	Iterator<String> driverStrings  = prefs.getStringList(DRIVER_PREFERENCES_KEY, new ArrayList<String>()).iterator();
    	while (driverStrings.hasNext()) {
    		drivers.add(new DriverInfo(driverStrings.next(), driverStrings.next(), new File(driverStrings.next())));
    	}
    	return drivers;
    }
    
    private void setDrivers(List<DriverInfo> drivers) {
    	Preferences prefs = PreferencesManager.getInstance().getPreferencesForSet(PREFERENCES_SET, DRIVER_PREFERENCES_KEY);
    	List<String>  prefsStringList = new ArrayList<String>();
    	for  (DriverInfo driver : drivers) {
    		prefsStringList.add(driver.getDescription());
    		prefsStringList.add(driver.getClassName());
    		prefsStringList.add(driver.getDriverLocation().getPath());
    	}
        prefs.clear();
    	prefs.putStringList(DRIVER_PREFERENCES_KEY, prefsStringList);
    }
    
    /* *******************************************************
     * impl
     */
    private JComponent createList() {
    	driverTableModel = new DriverTableModel(getDrivers(), jdbcRegistryTracker);
        table = new JTable(driverTableModel);
        return new JScrollPane(table);
    }
    
    private JComponent createButtons() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        add = new JButton("Add");
        add.addActionListener(new AddActionListener());
        panel.add(add);
        remove = new JButton("Remove");
        panel.add(remove);
        remove.addActionListener(new RemoveActionListener());
        edit = new JButton("Edit");
        panel.add(edit);
        edit.addActionListener(new EditActionListener());
        return panel;
    }

    private class AddActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            final EditorPanel editor = new EditorPanel(jdbcRegistryTracker);
            DriverInfo info = editor.askUserForDriverInfo();
            if (info != null) {
                driverTableModel.addDriver(info);
            }
        }
        
    }
    
    private class RemoveActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int [] rows = table.getSelectedRows();
            List<Integer> rowList = new ArrayList<Integer>();
            for (int row  : rows) {
                rowList.add(row);
            }
            driverTableModel.removeDrivers(rowList);
        }
        
    }
    
    private class EditActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
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
    
}
