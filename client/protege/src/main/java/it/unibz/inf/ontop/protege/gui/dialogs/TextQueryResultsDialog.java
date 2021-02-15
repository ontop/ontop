package it.unibz.inf.ontop.protege.gui.dialogs;

/*
 * #%L
 * ontop-protege
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

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.ui.workspace.Workspace;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import static java.awt.event.KeyEvent.VK_ESCAPE;

public class TextQueryResultsDialog extends JDialog {

	private static final long serialVersionUID = -200114540739796897L;
	
    public TextQueryResultsDialog(Workspace workspace, String title, String text, String processingTime) {
        setTitle(title);

        setLayout(new BorderLayout());
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(0, 5));

        JTextArea textArea = new JTextArea(text);
        textArea.setColumns(20);
        textArea.setEditable(false);
        textArea.setRows(5);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Dialog", Font.PLAIN, 12));
        mainPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        mainPanel.add(new JLabel(processingTime), BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        Action closeAction = new AbstractAction("Close") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispatchEvent(new WindowEvent(TextQueryResultsDialog.this, WindowEvent.WINDOW_CLOSING));
            }
        };

        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton(closeAction);
        controlPanel.add(closeButton);
        add(controlPanel, BorderLayout.SOUTH);

        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(VK_ESCAPE, 0), "cancel");
        ActionMap actionMap = getRootPane().getActionMap();
        actionMap.put("cancel", closeAction);

        getRootPane().setDefaultButton(closeButton);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setMinimumSize(new Dimension(700, 550));
        pack();
        JFrame protegeFrame = ProtegeManager.getInstance().getFrame(workspace);
        setLocationRelativeTo(protegeFrame);
    }
}
