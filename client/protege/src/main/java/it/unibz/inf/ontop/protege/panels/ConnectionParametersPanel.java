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
import javax.swing.border.EmptyBorder;
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

    private boolean notify = false;

    public ConnectionParametersPanel(OBDADataSource datasource) {
        super(new GridBagLayout());

        this.datasource = datasource;

        this.timer = new Timer(200, e -> handleTimer());

        KeyAdapter timerRestartKeyAdapter = new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                timer.restart();
            }
        };

        setBorder(new EmptyBorder(20,40,20, 40));

        add(new JLabel("Connection URL:"),
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 0, 3, 20), 0, 0));

        jdbcUrlField = new JTextField();
        jdbcUrlField.addKeyListener(timerRestartKeyAdapter);
        add(jdbcUrlField,
                new GridBagConstraints(1, 0, 1, 1, 1, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 0, 3, 0), 0, 0));

        add(new JLabel("Database username:"),
                new GridBagConstraints(0, 1, 1, 0, 0, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 0, 3, 20), 0, 0));

        usernameField = new JTextField();
        usernameField.addKeyListener(timerRestartKeyAdapter);
        add(usernameField,
                new GridBagConstraints(1, 1, 1, 1, 1, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 0, 3, 0), 0, 0));

        add(new JLabel("Database password:"),
                new GridBagConstraints(0, 2, 1, 1, 0, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 0, 3, 20), 0, 0));

        passwordField = new JPasswordField();
        passwordField.addKeyListener(timerRestartKeyAdapter);
        add(passwordField,
                new GridBagConstraints(1, 2, 1, 1, 1, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 0, 3, 0), 0, 0));

        add(new JLabel("JDBC driver class:"),
                new GridBagConstraints(0, 3, 1, 1, 0, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 0, 3, 20), 0, 0));

        ImmutableList<String> options = Stream.concat(
                Stream.of("select or type the JDBC Driver class..."),
                JDBCDriverTableModel.getDrivers().stream()
                        .map(JDBCDriverInfo::getClassName))
                .collect(ImmutableCollectors.toList());

        jdbcDriverComboBox = new JComboBox<>(new DefaultComboBoxModel<>(options.toArray(new String[0])));
        jdbcDriverComboBox.setEditable(true);
        jdbcDriverComboBox.addActionListener(evt -> timer.restart());
        jdbcDriverComboBox.addItemListener(evt -> timer.restart());
        add(jdbcDriverComboBox,
                new GridBagConstraints(1, 3, 1, 1, 1, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 0, 3, 0), 0, 0));

        add(DialogUtils.getButton(
                        "Test Connection",
                        "execute.png",
                        "Test settings by connecting to the server",
                        this::cmdTestConnectionActionPerformed),
                new GridBagConstraints(0, 4, 1, 1, 0, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.NONE,
                        new Insets(10, 0, 10, 0), 0, 0));

        connectionStatusLabel = new JLabel();
        add(connectionStatusLabel,
                new GridBagConstraints(0, 5, 2, 1, 0, 1,
                        GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                        new Insets(10, 0, 10, 0), 0, 0));
    }

    @Override
    public void changed() {
        notify = false;
        String driver = datasource.getDriver();
        if (driver == null || driver.isEmpty())
            jdbcDriverComboBox.setSelectedIndex(0);
        else
            jdbcDriverComboBox.setSelectedItem(driver);

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

        closeConnectionQuietly();

        String username = usernameField.getText();
        datasource.setUsername(username);
        String password = new String(passwordField.getPassword());
        datasource.setPassword(password);
        String driver = jdbcDriverComboBox.getSelectedIndex() == 0 ? "" : (String) jdbcDriverComboBox.getSelectedItem();
        datasource.setDriver(driver);
        String url = jdbcUrlField.getText();
        datasource.setURL(url);

        if (url.endsWith(" "))
            showError("<html>Warning:<br>URL ends with a space, which can cause connection problems.</html>");
        else if (driver.endsWith(" "))
            showError("<html>Warning:<br>driver class ends with a space, which can cause connection problems.</html>");
        else if (password.endsWith(" "))
            showError("<html>Warning:<br>password ends with a space, which can cause connection problems.</html>");
        else if (username.endsWith(" "))
            showError("<html>Warning:<br>username ends with a space, which can cause connection problems.</html>");
        else
            showError("");
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
                closeConnectionQuietly();
                Connection conn = datasource.getConnection();
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

    private static void closeConnectionQuietly() {
        JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();
        try {
            man.closeConnection();
        }
        catch (Exception ignore) {
        }
    }
}
