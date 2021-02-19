package it.unibz.inf.ontop.protege.query;

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

import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.SimpleDocumentListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.HTML_TAB;
import static java.awt.event.KeyEvent.*;

public class NewQueryDialog extends JDialog {

	private static final long serialVersionUID = -7101725310389493765L;
	
	private final static String GROUP_PROMPT = "Select or enter a new group name";
	private final static String NO_GROUP = "[No group]";

	private final QueryManager queryManager;

    private final JComboBox<String> groupComboBox;
    private final JTextField queryIdTextField;

	public NewQueryDialog(JComponent parent, QueryManager queryManager) {
		this.queryManager = queryManager;

        setTitle("New SPARQL Query");
        setModal(true);

        setLayout(new BorderLayout());
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(new JLabel("<html>Insert an ID and select or create a new group for this query.<br>" +
                        "Note: a new group ID cannot coincide with<br>" +
                        HTML_TAB + " an ID of a query without a group.</html>"),
                new GridBagConstraints(0, 0, 2, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(10, 0, 10, 0), 0, 0));

        mainPanel.add(new JLabel("Query ID:"),
                new GridBagConstraints(0, 1, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 4, 0), 0 ,0));

        queryIdTextField = new JTextField();
        mainPanel.add(queryIdTextField,
                new GridBagConstraints(1, 1, 1, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 4, 0), 0 ,0));

        mainPanel.add(new JLabel("Query group:"),
                new GridBagConstraints(0, 2, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 4, 0), 0 ,0));

        groupComboBox = new JComboBox<>();
        groupComboBox.addItem(GROUP_PROMPT);
        groupComboBox.addItem(NO_GROUP);
        queryManager.getRoot().getChildren().stream()
                .filter(e -> !e.isQuery())
                .forEach(g -> groupComboBox.addItem(g.getID()));
        groupComboBox.setEditable(true);

        mainPanel.add(groupComboBox,
                new GridBagConstraints(1, 2, 1, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 4, 0), 0 ,0));

        mainPanel.add(new Panel(), // gobbler
                new GridBagConstraints(1, 4, 1, 1, 1, 1,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 4, 0), 0 ,0));

        add(mainPanel, BorderLayout.CENTER);

        Action cancelAction = new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispatchEvent(new WindowEvent(NewQueryDialog.this, WindowEvent.WINDOW_CLOSING));
            }
        };

        Action okAction = new AbstractAction("OK") {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewQuery((String) groupComboBox.getSelectedItem(), queryIdTextField.getText().trim());
            }
        };

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton(cancelAction);
        controlPanel.add(cancelButton);
        JButton okButton = new JButton(okAction);
        okButton.setEnabled(false);
        controlPanel.add(okButton);
        add(controlPanel, BorderLayout.SOUTH);

        JTextComponent tc = (JTextComponent) groupComboBox.getEditor().getEditorComponent();
        SimpleDocumentListener okButtonEnabler = e -> okButton.setEnabled(
                !queryIdTextField.getText().trim().isEmpty()
                        && !GROUP_PROMPT.equals(tc.getText())
                        && !tc.getText().trim().isEmpty());
        queryIdTextField.getDocument().addDocumentListener(okButtonEnabler);
        tc.getDocument().addDocumentListener(okButtonEnabler);

        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(VK_ESCAPE, 0), "cancel");
        ActionMap actionMap = getRootPane().getActionMap();
        actionMap.put("cancel", cancelAction);

        getRootPane().setDefaultButton(okButton);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }

    private void createNewQuery(String groupId, String id) {
		try {
		    QueryManager.Item root = queryManager.getRoot();
            QueryManager.Item group = NO_GROUP.equals(groupId)
                    ? root
                    : root.getChild(groupId).orElseGet(() -> root.addGroupChild(groupId.trim()));

            group.addQueryChild(id, "");

            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
		catch (IllegalArgumentException e) {
            DialogUtils.showPrettyMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
	}
}
