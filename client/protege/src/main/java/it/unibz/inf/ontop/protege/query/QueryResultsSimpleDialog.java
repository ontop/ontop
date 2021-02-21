package it.unibz.inf.ontop.protege.query;

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

import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.OntopAbstractAction;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.ui.workspace.Workspace;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.getButton;
import static it.unibz.inf.ontop.protege.utils.DialogUtils.setUpAccelerator;

public class QueryResultsSimpleDialog extends JDialog {

	private static final long serialVersionUID = -200114540739796897L;
	
    public QueryResultsSimpleDialog(String title, String text, String processingTime) {
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

        OntopAbstractAction closeAction = DialogUtils.getStandardCloseWindowAction("Close", this);

        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton closeButton = getButton(closeAction);
        controlPanel.add(closeButton);
        add(controlPanel, BorderLayout.SOUTH);

        setUpAccelerator(getRootPane(), closeAction);
        getRootPane().setDefaultButton(closeButton);

        //setMinimumSize(new Dimension(700, 550));
    }
}
