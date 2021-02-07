package it.unibz.inf.ontop.protege.panels;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.utils.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.protege.osgi.jdbc.preferences.JDBCDriverInfo;
import org.protege.osgi.jdbc.preferences.JDBCDriverTableModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.HTML_TAB;

public class ConnectionParametersPanel extends JPanel implements OBDADataSource.Listener {

    private static final long serialVersionUID = 3506358479342412849L;

    private final OBDADataSource datasource;
    private final Timer timer;

    private final JLabel connectionStatusLabel;
    private final JPasswordField passwordField;
    private final JTextField usernameField;
    private final JComboBox<String> jdbcDriverComboBox;
    private final JTextField jdbcUrlField;

//    static private final Color COLOR_NOI18N = new Color(53, 113, 163);

    private boolean notify = false;

    public ConnectionParametersPanel(OBDADataSource datasource) {
        this.datasource = datasource;

        this.timer = new Timer(200, e -> handleTimer());

        KeyAdapter timerRestartKeyAdapter = new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                timer.restart();
            }
        };

        setLayout(new GridBagLayout());

        JPanel connectionParametersPanel = new JPanel(new GridBagLayout());

        JLabel jdbcUrlLabel = new JLabel("Connection URL:");
        connectionParametersPanel.add(jdbcUrlLabel,
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 10, 3, 10), 0, 0));

        jdbcUrlField = new JTextField();
        jdbcUrlField.addKeyListener(timerRestartKeyAdapter);
        connectionParametersPanel.add(jdbcUrlField,
                new GridBagConstraints(1, 0, 1, 1, 1, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 10, 3, 10), 0, 0));

        JLabel usernameLabel = new JLabel("Database username:");
        connectionParametersPanel.add(usernameLabel,
                new GridBagConstraints(0, 1, 1, 0, 0, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 10, 3, 10), 0, 0));

        usernameField = new JTextField();
        usernameField.addKeyListener(timerRestartKeyAdapter);
        connectionParametersPanel.add(usernameField,
                new GridBagConstraints(1, 1, 1, 1, 1, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 10, 3, 10), 0, 0));

        JLabel passwordLabel = new JLabel("Database password:");
        connectionParametersPanel.add(passwordLabel,
                new GridBagConstraints(0, 2, 1, 1, 0, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 10, 3, 10), 0, 0));

        passwordField = new JPasswordField();
        passwordField.addKeyListener(timerRestartKeyAdapter);
        connectionParametersPanel.add(passwordField,
                new GridBagConstraints(1, 2, 1, 1, 1, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 10, 3, 10), 0, 0));

        JLabel jdbcDriverLabel = new JLabel("JDBC driver class:");
        connectionParametersPanel.add(jdbcDriverLabel,
                new GridBagConstraints(0, 3, 1, 1, 0, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 10, 3, 10), 0, 0));

        ImmutableList<String> options = Stream.concat(
                Stream.of("select or type the JDBC Driver class..."),
                JDBCDriverTableModel.getDrivers().stream()
                        .map(JDBCDriverInfo::getClassName))
                .collect(ImmutableCollectors.toList());

        jdbcDriverComboBox = new JComboBox<>(new DefaultComboBoxModel<>(options.toArray(new String[0])));
        jdbcDriverComboBox.setEditable(true);
        jdbcDriverComboBox.addActionListener(evt -> timer.restart());
        jdbcDriverComboBox.addItemListener(evt -> timer.restart());
        connectionParametersPanel.add(jdbcDriverComboBox,
                new GridBagConstraints(1, 3, 1, 1, 1, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 10, 3, 10), 0, 0));

        JButton testConnectionButton = DialogUtils.getButton(
                "Test Connection",
                "execute.png",
                "Test settings by connecting to the server");
        testConnectionButton.addActionListener(this::cmdTestConnectionActionPerformed);
        connectionParametersPanel.add(testConnectionButton,
                new GridBagConstraints(0, 4, 1, 1, 0, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.NONE,
                        new Insets(10, 10, 10, 10), 0, 0));

        connectionStatusLabel = new JLabel();
        connectionParametersPanel.add(connectionStatusLabel,
                new GridBagConstraints(0, 5, 2, 1, 0, 1,
                        GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                        new Insets(10, 10, 10, 10), 0, 0));

        add(connectionParametersPanel,
                new GridBagConstraints(0, 1, 1, 1, 1, 1,
                        GridBagConstraints.LINE_START, GridBagConstraints.BOTH,
                        new Insets(20,30,20, 40), 0, 0));
    }

    @Override
    public void changed() {
        notify = false;
        String driver = datasource.getDriver();
        if (driver == null || driver.isEmpty()) {
            jdbcDriverComboBox.setSelectedIndex(0);
        }
        else {
            jdbcDriverComboBox.setSelectedItem(driver);
        }
        usernameField.setText(datasource.getUsername());
        passwordField.setText(datasource.getPassword());
        jdbcUrlField.setText(datasource.getURL());
        connectionStatusLabel.setText("");
        notify = true;
    }

    private void handleTimer() {
        if (!notify)
            return;

        timer.stop();

        JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();
        try {
            man.closeConnection();
        }
        catch (SQLException e) {
            // do nothing
        }

        String username = usernameField.getText();
        datasource.setUsername(username);
        String password = new String(passwordField.getPassword());
        datasource.setPassword(password);
        String driver = jdbcDriverComboBox.getSelectedIndex() == 0 ? "" : (String) jdbcDriverComboBox.getSelectedItem();
        datasource.setDriver(driver);
        String url = jdbcUrlField.getText();
        datasource.setURL(url);

        if (url.endsWith(" ")) {
            showError("<html>Warning:<br>URL ends with a space, which can cause connection problems.</html>");
        }
        else if (driver.endsWith(" ")) {
            showError("<html>Warning:<br>driver class ends with a space, which can cause connection problems.</html>");
        }
        else if (password.endsWith(" ")) {
            showError("<html>Warning:<br>password ends with a space, which can cause connection problems.</html>");
        }
        else if (username.endsWith(" ")) {
            showError("<html>Warning:<br>username ends with a space, which can cause connection problems.</html>");
        }
        else {
            showError("");
        }
    }

    private void showError(String s) {
        connectionStatusLabel.setForeground(Color.RED);
        connectionStatusLabel.setText(s);
    }

    private void cmdTestConnectionActionPerformed(ActionEvent evt) {

        connectionStatusLabel.setText("Establishing connection...");
        connectionStatusLabel.setForeground(Color.BLACK);

        String driver  = datasource.getDriver();
        if (driver.isEmpty()) {
            showError("Please, select or type in the JDBC driver class.");
        }
        else if (datasource.getURL().isEmpty()) {
            showError("Please, specify a connection URL.");
        }
        else {
            try {
                JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();
                try {
                    man.closeConnection();
                }
                catch (Exception e) {
                    // NO-OP
                }

                Connection conn = man.getConnection(datasource.getURL(), datasource.getUsername(), datasource.getPassword());
                if (conn == null)
                    throw new SQLException("Error connecting to the database");

                connectionStatusLabel.setForeground(Color.GREEN.darker());
                connectionStatusLabel.setText("Connection is OK");
            }
            catch (SQLException e) {
                String help = (e.getMessage().startsWith("No suitable driver"))
                        ? "<br><br>" +
                        "HINT: To setup JDBC drivers, open the Preference panel and go to the \"JDBC Drivers\" tab.<br>" +
                        HTML_TAB + "(Windows and Linux: Files &gt; Preferences..., Mac OS X: Protege &gt; Preferences...)<br>" +
                        "More information is on the Wiki:<br>" +
                        HTML_TAB +"<a href='https://github.com/ontop/ontop/wiki/FAQ'>https://github.com/ontop/ontop/wiki/FAQ</a>"
                        : "";

                showError(String.format("<html>%s (ERR-CODE: %s)%s</html>", e.getMessage(), e.getErrorCode(), help));
            }
       }
    }
}
