/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;
import javax.swing.KeyStroke;

public class EditableCellFocusAction extends WrappedAction implements ActionListener {
	
	private JTable table;

	/*
	 * Specify the component and KeyStroke for the Action we want to wrap
	 */
	public EditableCellFocusAction(JTable table, KeyStroke keyStroke) {
		super(table, keyStroke);
		this.table = table;
	}

	/*
	 * Provide the custom behaviour of the Action
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		invokeOriginalAction(e);
		
		int row = table.getSelectedRow();
		int column = table.getSelectedColumn();
		
		if (table.isCellEditable(row, column)) {
			table.editCellAt(row, column, e);
		}
	}
}
