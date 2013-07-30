/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.gui.action;

import it.unibz.krdb.obda.gui.swing.panel.OBDAModelStatisticsPanel;
import it.unibz.krdb.obda.gui.swing.utils.DialogUtils;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.utils.VirtualABoxStatistics;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;

public class ABoxStatisticsAction extends ProtegeAction {

	private static final long serialVersionUID = 3322509244957306932L;

	private OWLEditorKit editorKit = null;
	private OBDAModel obdaModel = null;
	private VirtualABoxStatistics statistics = null;
		
	@Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit)getEditorKit();		
		obdaModel = ((OBDAModelManager)editorKit.get(OBDAModelImpl.class.getName())).getActiveOBDAModel();
		statistics = new VirtualABoxStatistics(obdaModel);
	}

	@Override
	public void dispose() throws Exception {
		// Does nothing.
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		statistics.refresh();  // refresh the statistics every time users click the menu.
		
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setSize(520, 400);
		dialog.setLocationRelativeTo(null);
		dialog.setTitle("OBDA Model Statistics");
		
		OBDAModelStatisticsPanel pnlStatistics = new OBDAModelStatisticsPanel(statistics);
		JPanel pnlCommandButton = createButtonPanel(dialog); 
		dialog.setLayout(new BorderLayout());
		dialog.add(pnlStatistics, BorderLayout.CENTER);
		dialog.add(pnlCommandButton, BorderLayout.SOUTH);
		
		DialogUtils.installEscapeCloseOperation(dialog);
		
		dialog.pack();
		dialog.setVisible(true);
		
	}
	
	private JPanel createButtonPanel(final JDialog parent) {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		
		JButton cmdCloseInformation = new JButton();
		cmdCloseInformation.setText("Close Information");
		cmdCloseInformation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                parent.setVisible(false);
                parent.removeAll();
                parent.dispose();
            }
        });		
		panel.add(cmdCloseInformation);
		
		return panel;
	}
}
