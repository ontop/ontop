package it.unibz.inf.ontop.protege.jdbc;

import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;
import org.protege.osgi.jdbc.preferences.JdbcPreferencesPanelBundleActivator;
import org.protege.osgi.jdbc.JdbcRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.getKeyStrokeWithCtrlMask;
import static it.unibz.inf.ontop.protege.utils.DialogUtils.setUpAccelerator;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_ENTER;

public class JdbcPreferencesPanel extends OWLPreferencesPanel {
    private static final long serialVersionUID = 2892884854196959326L;

    public static final String PREFERENCES_SET = "org.protege.osgi.jdbc.prefs";
    public static final String DEFAULT_DRIVER_DIR = "driver.dir";
    public static final String DRIVER_PREFERENCES_KEY = "driver.list";

    private JTable table;
    private JdbcDriverTableModel driverTableModel;
    private ServiceTracker<?,?> jdbcRegistryTracker;

    @Override
    public void initialise() {
        BundleContext context = JdbcPreferencesPanelBundleActivator.getContext();
        jdbcRegistryTracker = new ServiceTracker<>(context, JdbcRegistry.class.getName(), null);

        setPreferredSize(new Dimension(620, 300));
        setLayout(new GridBagLayout());

        driverTableModel = new JdbcDriverTableModel(jdbcRegistryTracker);
        table = new JTable(driverTableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(350);
        table.getColumnModel().getColumn(3).setMaxWidth(50);
        add(new JScrollPane(table),
                new GridBagConstraints(0, 0, 3, 3, 1, 1,
                        GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(DialogUtils.getButton(addAction));
        controlPanel.add(DialogUtils.getButton(deleteAction));
        controlPanel.add(DialogUtils.getButton(editAction));
        add(controlPanel,
                new GridBagConstraints(0, 4, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> setActionsEnabled());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    editAction.actionPerformed(null);
            }
        });

        setActionsEnabled();

        setUpAccelerator(table, editAction);
        setUpAccelerator(table, deleteAction);
    }

    @Override
    public void dispose()  { /* NO-OP */ }

    @Override
    public void applyChanges() {
        driverTableModel.storeDriverInfoInPreferences();
    }

    private void setActionsEnabled() {
        boolean selectNonEmpty = !table.getSelectionModel().isSelectionEmpty();
        editAction.setEnabled(selectNonEmpty);
        deleteAction.setEnabled(selectNonEmpty);
    }

    private final OntopAbstractAction addAction = new OntopAbstractAction("Add",
            null, null, null) {
        @Override
        public void actionPerformed(ActionEvent e) {
            JdbcDriverEditSettingsDialog dialog = new JdbcDriverEditSettingsDialog(
                    getEditorKit().getWorkspace(),
                    jdbcRegistryTracker);
            DialogUtils.setLocationRelativeToProtegeAndOpen(getEditorKit(), dialog);
            dialog.getDriverInfo().ifPresent(i -> driverTableModel.addDriver(i));
        }
    };

    private final OntopAbstractAction editAction = new OntopAbstractAction("Edit",
            null,
            "Change the driver parameters",
            getKeyStrokeWithCtrlMask(VK_ENTER)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            JdbcDriverInfo info = driverTableModel.getDriver(row);
            JdbcDriverEditSettingsDialog dialog = new JdbcDriverEditSettingsDialog(
                    getEditorKit().getWorkspace(),
                    jdbcRegistryTracker,
                    info);
            DialogUtils.setLocationRelativeToProtegeAndOpen(getEditorKit(), dialog);
            dialog.getDriverInfo().ifPresent(i -> driverTableModel.replaceDriver(row, i));
        }
    };

    private final OntopAbstractAction deleteAction = new OntopAbstractAction("Delete",
            null,
            "Delete the driver",
            getKeyStrokeWithCtrlMask(VK_BACK_SPACE)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            JdbcDriverInfo info = driverTableModel.getDriver(row);

            if (!DialogUtils.confirmation(JdbcPreferencesPanel.this,
                    "Proceed deleting the " + info.getDescription() + " JDBC driver?",
                    "Delete JDBC Driver Confirmation"))
                return;

            driverTableModel.removeDrivers(row);
        }
    };
}
