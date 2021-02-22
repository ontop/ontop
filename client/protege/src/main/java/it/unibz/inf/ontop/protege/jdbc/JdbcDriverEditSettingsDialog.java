package it.unibz.inf.ontop.protege.jdbc;

import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;
import org.osgi.util.tracker.ServiceTracker;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.osgi.jdbc.JdbcRegistry;
import org.protege.osgi.jdbc.JdbcRegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Optional;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.getButton;
import static it.unibz.inf.ontop.protege.utils.DialogUtils.setUpAccelerator;
import static javax.swing.SwingConstants.TOP;

public class JdbcDriverEditSettingsDialog extends JDialog {
    private static final long serialVersionUID = -8958695683502439830L;

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcDriverEditSettingsDialog.class);

    private final ServiceTracker<?,?> jdbcRegistryTracker;
    private final Preferences prefs;

    private final JLabel statusLabel;
    private final JTextField nameField;
    private final JComboBox<String> classField;
    private final JTextField fileField;

    private File defaultDir;

    private JdbcDriverInfo result;
    
    private static final int GAP = 2;

    public JdbcDriverEditSettingsDialog(Container parent, ServiceTracker<?,?> jdbcRegistryTracker) {

        this.jdbcRegistryTracker = jdbcRegistryTracker;

        prefs = PreferencesManager.getInstance().getPreferencesForSet(JdbcPreferencesPanel.PREFERENCES_SET, JdbcPreferencesPanel.DEFAULT_DRIVER_DIR);
        String dirName = prefs.getString(JdbcPreferencesPanel.DEFAULT_DRIVER_DIR, null);
        if (dirName != null) {
            defaultDir = new File(dirName);
            if (!defaultDir.exists())
                defaultDir = null;
        }

        setTitle("JDBC Driver Parameters");
        setModal(true);

        setLayout(new BorderLayout());
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(new JLabel("Description:"),
            new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(GAP, GAP, GAP, GAP), 0, 0));

        nameField = new JTextField();
        mainPanel.add(nameField,
                new GridBagConstraints(1, 0, 2, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        mainPanel.add(new JLabel("Class name:"),
                new GridBagConstraints(0, 1, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        classField = new JComboBox<>(new String[] {
                "select or type the JDBC Driver's class...",
                "org.postgresql.Driver",
                "com.mysql.jdbc.Driver",
                "org.h2.Driver",
                "com.ibm.db2.jcc.DB2Driver",
                "oracle.jdbc.driver.OracleDriver",
                "com.microsoft.sqlserver.jdbc.SQLServerDriver" });
        classField.setEditable(true);
        mainPanel.add(classField,
                new GridBagConstraints(1, 1, 2, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        mainPanel.add(new JLabel("Driver file (jar):"),
                new GridBagConstraints(0, 2, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        fileField = new JTextField();
        mainPanel.add(fileField,
                new GridBagConstraints(1, 2, 1, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        mainPanel.add(getButton(browseAction),
                new GridBagConstraints(2, 2, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 0), 0, 0));

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setVerticalAlignment(TOP);
        mainPanel.add(statusLabel,
                new GridBagConstraints(0, 3, 3, 1, 1, 1,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(GAP, GAP, GAP, GAP), 0, 0));

        add(mainPanel, BorderLayout.CENTER);

        OntopAbstractAction cancelAction = DialogUtils.getStandardCloseWindowAction(DialogUtils.CANCEL_BUTTON_TEXT, this);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.add(getButton(cancelAction));
        JButton okButton = getButton(okAction);
        controlPanel.add(okButton);
        add(controlPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);
        setUpAccelerator(getRootPane(), cancelAction);
    }

    public JdbcDriverEditSettingsDialog(Container parent, ServiceTracker<?,?> jdbcRegistryTracker, JdbcDriverInfo info) {
        this(parent, jdbcRegistryTracker);

        nameField.setText(info.getDescription());
        classField.setSelectedItem(info.getClassName());
        fileField.setText(info.getDriverPath());
    }

    private final OntopAbstractAction browseAction = new OntopAbstractAction("Browse", null, null, null) {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc  = new JFileChooser(defaultDir);
            if (fc.showOpenDialog(JdbcDriverEditSettingsDialog.this) != JFileChooser.APPROVE_OPTION)
                return;

            File file = fc.getSelectedFile();
            fileField.setText(file.getPath());
            defaultDir = file.getParentFile();
            prefs.putString(JdbcPreferencesPanel.DEFAULT_DRIVER_DIR, defaultDir.getAbsolutePath());
        }
    };

    private final OntopAbstractAction okAction = new OntopAbstractAction(DialogUtils.OK_BUTTON_TEXT, null, null, null) {
        @Override
        public void actionPerformed(ActionEvent e) {
            registerDriverInfoAndClose(
                    nameField.getText(),
                    (String) classField.getSelectedItem(),
                    new File(fileField.getText()));
        }
    };

    private void registerDriverInfoAndClose(String description, String className, File file) {
        try {
            jdbcRegistryTracker.open();
            for (Object o : jdbcRegistryTracker.getServices()) {
                JdbcRegistry registry = (JdbcRegistry) o;
                try {
                    registry.addJdbcDriver(className, file.toURI().toURL());
                    result = new JdbcDriverInfo(description, className, file);
                    dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                }
                catch (JdbcRegistryException e) {
                    LOGGER.info("Could not add driver to jdbc", e);
                    statusLabel.setText(e.getMessage());
                }
                catch (MalformedURLException e) {
                    LOGGER.error("Unexpected URL misconfiguration", e);
                    statusLabel.setText(e.getMessage());
                }
            }
        }
        finally {
            jdbcRegistryTracker.close();
        }
    }

    public Optional<JdbcDriverInfo> getDriverInfo() {
        return Optional.ofNullable(result);
    }
}
