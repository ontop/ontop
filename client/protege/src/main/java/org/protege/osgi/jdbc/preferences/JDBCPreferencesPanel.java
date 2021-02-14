package org.protege.osgi.jdbc.preferences;

import it.unibz.inf.ontop.protege.gui.dialogs.JDBCDriverEditSettingsDialog;
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

public class JDBCPreferencesPanel extends OWLPreferencesPanel {
    private static final long serialVersionUID = 2892884854196959326L;

    public static final String PREFERENCES_SET = "org.protege.osgi.jdbc.prefs";
    public static final String DEFAULT_DRIVER_DIR = "driver.dir";
    public static final String DRIVER_PREFERENCES_KEY = "driver.list";

    private JTable table;
    private JDBCDriverTableModel driverTableModel;
    private ServiceTracker<?,?> jdbcRegistryTracker;

    @Override
    public void initialise() throws Exception {
        BundleContext context = JDBCPreferencesPanelBundleActivator.getContext();
        jdbcRegistryTracker = new ServiceTracker<>(context, JdbcRegistry.class.getName(), null);

        setPreferredSize(new Dimension(620, 300));
        setLayout(new GridBagLayout());

        driverTableModel = new JDBCDriverTableModel(jdbcRegistryTracker);
        table = new JTable(driverTableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(350);
        table.getColumnModel().getColumn(3).setMaxWidth(50);
        add(new JScrollPane(table),
                new GridBagConstraints(0, 0, 3, 3, 1, 1,
                        GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        JButton add = new JButton("Add");
        add.addActionListener(this::cmdAddDriver);
        controlPanel.add(add);
        JButton remove = new JButton("Remove");
        controlPanel.add(remove);
        remove.addActionListener(this::cmdRemoveDriver);
        JButton edit = new JButton("Edit");
        controlPanel.add(edit);
        edit.addActionListener(this::cmdEditDriver);
        add(controlPanel,
                new GridBagConstraints(0, 4, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));

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
    public void dispose()  { /* NO-OP */ }

    @Override
    public void applyChanges() {
        driverTableModel.storeDriverInfoInPreferences();
    }

    private void cmdAddDriver(ActionEvent e) {
        JDBCDriverEditSettingsDialog dialog = new JDBCDriverEditSettingsDialog(this, jdbcRegistryTracker);
        Optional<JDBCDriverInfo> info = dialog.showDialog();
        info.ifPresent(i -> driverTableModel.addDriver(i));
    }

    private void cmdEditDriver(ActionEvent e) {
        int row = table.getSelectedRow();
        JDBCDriverInfo info = driverTableModel.getDriver(row);
        JDBCDriverEditSettingsDialog editor = new JDBCDriverEditSettingsDialog(this, jdbcRegistryTracker, info);
        Optional<JDBCDriverInfo> newInfo = editor.showDialog();
        newInfo.ifPresent(i -> driverTableModel.replaceDriver(row, i));
    }

    private void cmdRemoveDriver(ActionEvent e) {
        int row = table.getSelectedRow();
        JDBCDriverInfo info = driverTableModel.getDriver(row);

        if (JOptionPane.showConfirmDialog(
                this,
                "Proceed deleting the " + info.getDescription() + " JDBC driver?",
                "Delete JDBC Driver Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
            return;

        driverTableModel.removeDrivers(row);
    }
}
