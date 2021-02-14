package it.unibz.inf.ontop.protege.gui.dialogs;

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
import it.unibz.inf.ontop.protege.core.QueryManager;
import it.unibz.inf.ontop.protege.utils.SimpleDocumentListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import static java.awt.event.KeyEvent.*;

public class NewQueryDialog extends JDialog {

	private static final long serialVersionUID = -7101725310389493765L;
	
	private final static String NEW_GROUP = "New group...";
	private final static String NO_GROUP = "No group";

	private final QueryManager queryManager;

    private final JComboBox<String> cmbQueryGroup;
    private final JTextField txtGroupName;
    private final JTextField txtQueryID;

	public NewQueryDialog(JComponent parent, QueryManager queryManager) {
		this.queryManager = queryManager;

        setTitle("New SPARQL Query");
        setModal(true);

        setLayout(new BorderLayout());
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(new JLabel("<html>Insert an ID and select/create a group for this query.</html>"),
                new GridBagConstraints(0, 0, 2, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(10, 0, 10, 0), 0, 0));

        mainPanel.add(new JLabel("Query ID:"),
                new GridBagConstraints(0, 1, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 4, 0), 0 ,0));

        txtQueryID = new JTextField();
        mainPanel.add(txtQueryID,
                new GridBagConstraints(1, 1, 1, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 4, 0), 0 ,0));

        mainPanel.add(new JLabel("Query group:"),
                new GridBagConstraints(0, 2, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 4, 0), 0 ,0));

        cmbQueryGroup = new JComboBox<>(new DefaultComboBoxModel<>(new String[0]));
        cmbQueryGroup.insertItemAt(NO_GROUP, cmbQueryGroup.getItemCount());
        for (QueryManager.Group group : queryManager.getGroups()) {
            if (!group.isDegenerate())
                cmbQueryGroup.insertItemAt(group.getID(), cmbQueryGroup.getItemCount());
        }
        cmbQueryGroup.insertItemAt(NEW_GROUP, cmbQueryGroup.getItemCount());
        cmbQueryGroup.setSelectedItem(NO_GROUP);

        mainPanel.add(cmbQueryGroup,
                new GridBagConstraints(1, 2, 1, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 4, 0), 0 ,0));

        mainPanel.add(new JLabel("Group name:"),
                new GridBagConstraints(0, 3, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 4, 0), 0 ,0));

        txtGroupName = new JTextField();
        txtGroupName.setEnabled(false);
        cmbQueryGroup.addItemListener(e -> txtGroupName.setEnabled(e.getItem().equals(NEW_GROUP)));
        mainPanel.add(txtGroupName,
                new GridBagConstraints(1, 3, 1, 1, 1, 0,
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
                cmdCreateNewActionPerformed(e);
            }
        };

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton(cancelAction);
        controlPanel.add(cancelButton);
        JButton okButton = new JButton(okAction);
        okButton.setEnabled(false);
        controlPanel.add(okButton);
        add(controlPanel, BorderLayout.SOUTH);

        txtQueryID.getDocument().addDocumentListener((SimpleDocumentListener)
                e -> okButton.setEnabled(!txtQueryID.getText().trim().isEmpty()));

        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(VK_ESCAPE, 0), "cancel");
        ActionMap actionMap = getRootPane().getActionMap();
        actionMap.put("cancel", cancelAction);

        getRootPane().setDefaultButton(okButton);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }

    private void cmdCreateNewActionPerformed(ActionEvent evt) {
		try {
            String id = txtQueryID.getText().trim();
            String groupId = (String) cmbQueryGroup.getSelectedItem();
            if (NO_GROUP.equals(groupId)) {
                queryManager.addQuery(id, "");
            }
            else {
                QueryManager.Group group;
                if (NEW_GROUP.equals(groupId)) {
                    String newGroupId = txtGroupName.getText().trim();
                    group = queryManager.addGroup(newGroupId);
                }
                else {
                    group = queryManager.getGroup(groupId);
                }
                queryManager.addQuery(group, id, "");
            }
        }
		catch (IllegalArgumentException e) {
            DialogUtils.showPrettyMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
}
