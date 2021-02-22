package it.unibz.inf.ontop.protege.jdbc;

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

import static java.awt.event.KeyEvent.*;
import static javax.swing.SwingConstants.TOP;

public class JDBCDriverEditSettingsDialog extends JDialog {
    private static final long serialVersionUID = -8958695683502439830L;

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCDriverEditSettingsDialog.class);

    private final ServiceTracker<?,?> jdbcRegistryTracker;
    private final Preferences prefs;

    private final JLabel statusLabel;
    private final JTextField nameField;
    private final JComboBox<String> classField;
    private final JTextField fileField;

    private File defaultDir;

    private JDBCDriverInfo result;
    
    private static final int GAP = 2;

    public JDBCDriverEditSettingsDialog(Container parent, ServiceTracker<?,?> jdbcRegistryTracker) {

        this.jdbcRegistryTracker = jdbcRegistryTracker;

        prefs = PreferencesManager.getInstance().getPreferencesForSet(JDBCPreferencesPanel.PREFERENCES_SET, JDBCPreferencesPanel.DEFAULT_DRIVER_DIR);
        String dirName = prefs.getString(JDBCPreferencesPanel.DEFAULT_DRIVER_DIR, null);
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

        Action browseAction = new AbstractAction("Browse") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc  = new JFileChooser(defaultDir);
                if (fc.showOpenDialog(JDBCDriverEditSettingsDialog.this) != JFileChooser.APPROVE_OPTION)
                    return;

                File file = fc.getSelectedFile();
                fileField.setText(file.getPath());
                defaultDir = file.getParentFile();
                prefs.putString(JDBCPreferencesPanel.DEFAULT_DRIVER_DIR, defaultDir.getAbsolutePath());
            }
        };

        mainPanel.add(new JButton(browseAction),
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

        Action cancelAction = new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = null;
                dispatchEvent(new WindowEvent(JDBCDriverEditSettingsDialog.this, WindowEvent.WINDOW_CLOSING));
            }
        };

        Action okAction = new AbstractAction("OK") {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerDriverInfoAndClose(
                        nameField.getText(),
                        (String) classField.getSelectedItem(),
                        new File(fileField.getText()));
            }
        };

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton(cancelAction);
        controlPanel.add(cancelButton);
        JButton okButton = new JButton(okAction);
        controlPanel.add(okButton);
        add(controlPanel, BorderLayout.SOUTH);

        InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(VK_ESCAPE, 0), "cancel");
        ActionMap actionMap = mainPanel.getActionMap();
        actionMap.put("cancel", cancelAction);

        getRootPane().setDefaultButton(okButton);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }

    public JDBCDriverEditSettingsDialog(Container parent, ServiceTracker<?,?> jdbcRegistryTracker, JDBCDriverInfo info) {
        this(parent, jdbcRegistryTracker);

        nameField.setText(info.getDescription());
        classField.setSelectedItem(info.getClassName());
        fileField.setText(info.getDriverPath());
    }


    private void registerDriverInfoAndClose(String description, String className, File file) {
        try {
            jdbcRegistryTracker.open();
            for (Object o : jdbcRegistryTracker.getServices()) {
                JdbcRegistry registry = (JdbcRegistry) o;
                try {
                    registry.addJdbcDriver(className, file.toURI().toURL());
                    result = new JDBCDriverInfo(description, className, file);
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

    public Optional<JDBCDriverInfo> showDialog() {
        setVisible(true);
        return Optional.ofNullable(result);
    }
}
