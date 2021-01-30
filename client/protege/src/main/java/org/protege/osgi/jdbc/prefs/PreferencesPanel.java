package org.protege.osgi.jdbc.prefs;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;
import org.protege.osgi.jdbc.JdbcRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class PreferencesPanel extends OWLPreferencesPanel {
    private static final long serialVersionUID = 2892884854196959326L;

    public static final String PREFERENCES_SET = "org.protege.osgi.jdbc.prefs";
    public static final String DEFAULT_DRIVER_DIR = "driver.dir";
    public static final String DRIVER_PREFERENCES_KEY = "driver.list";

    private JTable table;
    private JDBCDriverTableModel driverTableModel;
    private ServiceTracker<?,?> jdbcRegistryTracker;

    @Override
    public void initialise() throws Exception {
        BundleContext context = Activator.getContext();
        jdbcRegistryTracker = new ServiceTracker<>(context, JdbcRegistry.class.getName(), null);

        setPreferredSize(new Dimension(620, 300));
        setLayout(new GridBagLayout());

        driverTableModel = new JDBCDriverTableModel(jdbcRegistryTracker);
        table = new JTable(driverTableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(350);
        table.getColumnModel().getColumn(3).setMaxWidth(50);
        GridBagConstraints listConstraints = new GridBagConstraints();
        listConstraints.fill = GridBagConstraints.BOTH;
        listConstraints.gridx = 0;
        listConstraints.gridy = 0;
        listConstraints.gridwidth = 3;
        listConstraints.gridheight = 3;
        listConstraints.weightx = 1;
        listConstraints.weighty = 1;
        add(new JScrollPane(table), listConstraints);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton add = new JButton("Add");
        add.addActionListener(this::cmdAddDriver);
        panel.add(add);
        JButton remove = new JButton("Remove");
        panel.add(remove);
        remove.addActionListener(this::cmdRemoveDriver);
        JButton edit = new JButton("Edit");
        panel.add(edit);
        edit.addActionListener(this::cmdEditDriver);
        GridBagConstraints buttonsConstraints = new GridBagConstraints();
        buttonsConstraints.gridx = 0;
        buttonsConstraints.gridy = 4;
        add(panel, buttonsConstraints);

        edit.setEnabled(false);
        remove.setEnabled(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel selectionModel = table.getSelectionModel();
        selectionModel.addListSelectionListener(e -> {
            boolean selectNonEmpty = !selectionModel.isSelectionEmpty();
            edit.setEnabled(selectNonEmpty);
            remove.setEnabled(selectNonEmpty);
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    cmdEditDriver(null);
            }
        });
    }

    @Override
    public void dispose()  { }

    @Override
    public void applyChanges() {
        driverTableModel.store();
    }

    private void cmdAddDriver(ActionEvent e) {
        EditJDBCDriverSettingsDialog editor = new EditJDBCDriverSettingsDialog(this, jdbcRegistryTracker);
        Optional<JDBCDriverInfo> info = editor.askUserForDriverInfo();
        info.ifPresent(i -> driverTableModel.addDriver(i));
    }

    private void cmdEditDriver(ActionEvent e) {
        int row = table.getSelectedRow();
        JDBCDriverInfo info = driverTableModel.getDriver(row);
        EditJDBCDriverSettingsDialog editor = new EditJDBCDriverSettingsDialog(this, jdbcRegistryTracker);
        Optional<JDBCDriverInfo> newInfo = editor.askUserForDriverInfo(info);
        newInfo.ifPresent(i -> driverTableModel.replaceDriver(row, i));
    }

    private void cmdRemoveDriver(ActionEvent e) {
        int row = table.getSelectedRow();
        JDBCDriverInfo info = driverTableModel.getDriver(row);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Proceed deleting the " + info.getDescription() + " driver?",
                "Delete Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        driverTableModel.removeDrivers(row);
    }
}
