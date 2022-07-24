package it.unibz.inf.ontop.protege.connection;

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
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.OBDAModelManagerListener;
import it.unibz.inf.ontop.protege.jdbc.JdbcDriverInfo;
import it.unibz.inf.ontop.protege.jdbc.JdbcDriverTableModel;
import it.unibz.inf.ontop.protege.utils.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.*;
import static java.awt.event.KeyEvent.VK_T;

public class DataSourcePanel extends JPanel implements OBDAModelManagerListener {

    private static final long serialVersionUID = 3506358479342412849L;

    private final OBDAModelManager obdaModelManager;

    private final JLabel connectionStatusLabel;
    private final JPasswordField passwordField;
    private final JTextField usernameField;
    private final JComboBox<String> jdbcDriverComboBox;
    private final JTextField jdbcUrlField;

    private boolean fetchingInfo = false;

    public DataSourcePanel(OWLEditorKit editorKit) {
        super(new GridBagLayout());

        this.obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);

        setBorder(new EmptyBorder(20,40,20, 40));

        add(new JLabel("Connection URL:"),
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 0, 3, 20), 0, 0));

        jdbcUrlField = new JTextField();
        jdbcUrlField.getDocument().addDocumentListener((SimpleDocumentListener) evt -> documentChange());
        add(jdbcUrlField,
                new GridBagConstraints(1, 0, 1, 1, 1, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 0, 3, 0), 0, 0));

        add(new JLabel("Database username:"),
                new GridBagConstraints(0, 1, 1, 0, 0, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 0, 3, 20), 0, 0));

        usernameField = new JTextField();
        usernameField.getDocument().addDocumentListener((SimpleDocumentListener) evt -> documentChange());
        add(usernameField,
                new GridBagConstraints(1, 1, 1, 1, 1, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 0, 3, 0), 0, 0));

        add(new JLabel("Database password:"),
                new GridBagConstraints(0, 2, 1, 1, 0, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 0, 3, 20), 0, 0));

        passwordField = new JPasswordField();
        passwordField.getDocument().addDocumentListener((SimpleDocumentListener) evt -> documentChange());
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
                JdbcDriverTableModel.getDriverInfoFromPreferences().stream()
                        .map(JdbcDriverInfo::getClassName))
                .collect(ImmutableCollectors.toList());

        jdbcDriverComboBox = new JComboBox<>(new DefaultComboBoxModel<>(options.toArray(new String[0])));
        jdbcDriverComboBox.setEditable(true);
        jdbcDriverComboBox.addActionListener(evt -> documentChange());
        jdbcDriverComboBox.addItemListener(evt -> documentChange());
        add(jdbcDriverComboBox,
                new GridBagConstraints(1, 3, 1, 1, 1, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                        new Insets(3, 0, 3, 0), 0, 0));

        add(getButton(testAction),
                new GridBagConstraints(0, 4, 1, 1, 0, 0,
                        GridBagConstraints.NORTH, GridBagConstraints.NONE,
                        new Insets(10, 0, 10, 0), 0, 0));

        connectionStatusLabel = new JLabel();
        add(connectionStatusLabel,
                new GridBagConstraints(0, 5, 2, 1, 0, 1,
                        GridBagConstraints.NORTH, GridBagConstraints.BOTH,
                        new Insets(10, 0, 10, 0), 0, 0));

        setUpAccelerator(this, testAction);
        activeOntologyChanged(obdaModelManager.getCurrentOBDAModel());
    }

    private final OntopAbstractAction testAction = new OntopAbstractAction("Test Connection",
            "execute.png",
            "Test settings by connecting to the server",
            getKeyStrokeWithCtrlMask(VK_T)) {

        @Override
        public void actionPerformed(ActionEvent evt) {
            connectionStatusLabel.setForeground(Color.BLACK);
            connectionStatusLabel.setText("Establishing connection...");

            DataSource dataSource =  obdaModelManager.getCurrentOBDAModel().getDataSource();
            try (Connection ignored = dataSource.getConnection()) {
                connectionStatusLabel.setForeground(Color.GREEN.darker());
                connectionStatusLabel.setText("Connection is OK");
            }
            catch (SQLException e) {
                String help = (e.getMessage().startsWith("No suitable driver"))
                        ? "<br><br>" +
                        "HINT: To setup JDBC drivers, open the Preference panel and go to the \"JDBC Drivers\" tab.<br>" +
                        HTML_TAB + "(Windows and Linux: Files &gt; Preferences..., Mac OS X: Protege &gt; Preferences...)" 
                        : "";

                connectionStatusLabel.setForeground(Color.RED);
                connectionStatusLabel.setText(String.format("<html>%s (ERR-CODE: %s)%s</html>", e.getMessage(), e.getErrorCode(), help));
            }
        }
    };

    @Override // see DataSourceView
    public void activeOntologyChanged(OBDAModel obdaModel) {
        DataSource datasource = obdaModel.getDataSource();

        fetchingInfo = true;
        String driver = datasource.getDriver();
        if (driver.isEmpty())
            jdbcDriverComboBox.setSelectedIndex(0);
        else
            jdbcDriverComboBox.setSelectedItem(driver);

        usernameField.setText(datasource.getUsername());
        passwordField.setText(datasource.getPassword());
        jdbcUrlField.setText(datasource.getURL());
        fetchingInfo = false;

        documentChange();
    }

    private void documentChange() {
        if (fetchingInfo)
            return;  // do nothing if changes are in activeOntologyChanged

        char[] password = passwordField.getPassword();
        String driver = jdbcDriverComboBox.getSelectedIndex() == 0 ? "" : (String) jdbcDriverComboBox.getSelectedItem();

        connectionStatusLabel.setForeground(Color.RED);
        if (jdbcUrlField.getText().endsWith(" "))
            connectionStatusLabel.setText("<html>Warning:<br>Connection URL ends with a space, " +
                    "which can cause connection problems.</html>");

        else if (usernameField.getText().endsWith(" "))
            connectionStatusLabel.setText("<html>Warning:<br>Database username ends with a space, " +
                    "which can cause connection problems.</html>");

        else if (password.length > 0 && password[password.length - 1] == ' ')
            connectionStatusLabel.setText("<html>Warning:<br>Database password ends with a space, " +
                    "which can cause connection problems.</html>");

        else if (driver.endsWith(" "))
            connectionStatusLabel.setText("<html>Warning:<br>JDBC driver class ends with a space, " +
                    "which can cause connection problems.</html>");

        else
            connectionStatusLabel.setText("");

        DataSource datasource = obdaModelManager.getCurrentOBDAModel().getDataSource();
        datasource.set(jdbcUrlField.getText(), usernameField.getText(), new String(password), driver);

        testAction.setEnabled(!datasource.getURL().isEmpty() && !datasource.getDriver().isEmpty());
    }
}
