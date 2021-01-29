package org.protege.osgi.jdbc.prefs;

import org.osgi.util.tracker.ServiceTracker;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.osgi.jdbc.JdbcRegistry;
import org.protege.osgi.jdbc.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Optional;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.centerDialogWRTParent;

public class EditJDBCDriverSettingsDialog extends JDialog {
    private static final long serialVersionUID = -8958695683502439830L;

    private final Logger log = LoggerFactory.getLogger(EditJDBCDriverSettingsDialog.class);

    private final ServiceTracker<?,?> jdbcRegistryTracker;
    
    private final JLabel status;
    private final JTextField nameField;
    private final JComboBox<String> classField;
    private final JTextField fileField;

    private JDBCDriverInfo info;
    
    private final Preferences prefs;

    private File defaultDir;

    public EditJDBCDriverSettingsDialog(Container parent, ServiceTracker<?,?> jdbcRegistryTracker) {

        this.jdbcRegistryTracker = jdbcRegistryTracker;

        prefs = PreferencesManager.getInstance().getPreferencesForSet(PreferencesPanel.PREFERENCES_SET, PreferencesPanel.DEFAULT_DRIVER_DIR);
        String dirName = prefs.getString(PreferencesPanel.DEFAULT_DRIVER_DIR, null);
        if (dirName != null) {
            defaultDir = new File(dirName);
            if (!defaultDir.exists())
                defaultDir = null;
        }

        setTitle("JDBC Driver Parameters");
        setModal(true);

        setLayout(new BorderLayout());

        status = new JLabel(" ");
        add(status, BorderLayout.NORTH);

        JPanel centerPane = new JPanel();

        GroupLayout layout = new GroupLayout(centerPane);
        centerPane.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel nameLabel = new JLabel("Description:");
        nameField = new JTextField();

        JLabel classLabel = new JLabel("Class Name:");
        classField = new JComboBox<>(new String[] {
                "select or type the JDBC Driver's class...",
                "org.postgresql.Driver",
                "com.mysql.jdbc.Driver",
                "org.h2.Driver",
                "com.ibm.db2.jcc.DB2Driver",
                "oracle.jdbc.driver.OracleDriver",
                "com.microsoft.sqlserver.jdbc.SQLServerDriver" });
        classField.setEditable(true);

        JLabel fileLabel = new JLabel("Driver File (jar):");
        fileField = new JTextField();
        JLabel sample = new JLabel("/home/tredmond/dev/workspaces/protege4");
        Dimension size = sample.getPreferredSize();
        fileField.setPreferredSize(size);

        JButton fileButton = new JButton("Browse");
        centerPane.add(fileButton);
        fileButton.addActionListener(this::cmdBrowse);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(nameLabel)
                                .addComponent(classLabel)
                                .addComponent(fileLabel))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(nameField)
                                .addComponent(classField)
                                .addComponent(fileField))
                        .addComponent(fileButton));

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(nameLabel)
                                .addComponent(nameField))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(classLabel)
                                .addComponent(classField))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(fileLabel)
                                .addComponent(fileField)
                                .addComponent(fileButton)));

        add(centerPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton ok = new JButton("Ok");
        ok.addActionListener(this::cmdOK);
        panel.add(ok);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(this::cmdCancel);
        panel.add(cancel);
        add(panel, BorderLayout.SOUTH);

        pack();

        centerDialogWRTParent(parent, this);
    }

    private void cmdBrowse(ActionEvent evt) {
        JFileChooser fc  = new JFileChooser(defaultDir);
        int retVal = fc.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION)  {
            File file = fc.getSelectedFile();
            fileField.setText(file.getPath());
            defaultDir = file.getParentFile();
            prefs.putString(PreferencesPanel.DEFAULT_DRIVER_DIR, defaultDir.getAbsolutePath());
        }
    }

    private void cmdCancel(ActionEvent evt) {
        info = null;
        dispose();
    }

    private void cmdOK(ActionEvent evt) {
        String className = (String) classField.getSelectedItem();
        File f  = new File(fileField.getText());
        try {
            jdbcRegistryTracker.open();
            for (Object o : jdbcRegistryTracker.getServices()) {
                JdbcRegistry registry = (JdbcRegistry) o;
                try {
                    registry.addJdbcDriver(className, f.toURI().toURL());
                    info = new JDBCDriverInfo(nameField.getText(), className, f);
                    dispose();
                }
                catch (RegistryException e) {
                    log.info("Could not add driver to jdbc", e);
                    status.setText(e.getMessage());
                }
                catch (MalformedURLException e) {
                    log.error("Unexpected URL misconfiguration", e);
                    status.setText(e.getMessage());
                }
            }
        }
        finally {
            jdbcRegistryTracker.close();
        }
    }

    public Optional<JDBCDriverInfo> askUserForDriverInfo() {
        setVisible(true);
        return Optional.ofNullable(info);
    }

    public Optional<JDBCDriverInfo> askUserForDriverInfo(JDBCDriverInfo oldInfo) {
        nameField.setText(oldInfo.getDescription());
        classField.setSelectedItem(oldInfo.getClassName());
        fileField.setText(oldInfo.getDriverPath());
        setVisible(true);
        return Optional.ofNullable(info);
    }
}
