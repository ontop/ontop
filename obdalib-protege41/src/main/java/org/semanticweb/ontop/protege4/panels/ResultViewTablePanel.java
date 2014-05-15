package org.semanticweb.ontop.protege4.panels;

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.table.TableModel;

import org.semanticweb.ontop.protege4.gui.IconLoader;
import org.semanticweb.ontop.protege4.gui.action.OBDADataQueryAction;
import org.semanticweb.ontop.protege4.gui.action.OBDASaveQueryResultToFileAction;
import org.semanticweb.ontop.protege4.gui.treemodels.IncrementalResultSetTableModel;

public class ResultViewTablePanel extends javax.swing.JPanel {

	private static final long serialVersionUID = -8494558136315031084L;

	private OBDADataQueryAction countAllTuplesAction;
	private OBDADataQueryAction countAllTuplesActionEQL;
	private QueryInterfacePanel querypanel;
	private OBDASaveQueryResultToFileAction saveToFileAction;
	
	/** 
	 * Creates new form ResultViewTablePanel 
	 */
	public ResultViewTablePanel(QueryInterfacePanel panel) {
		querypanel = panel;
		initComponents();
		addPopUpMenu();
	}

	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrQueryResult = new javax.swing.JScrollPane();
        tblQueryResult = new javax.swing.JTable();
        pnlCommandButton = new javax.swing.JPanel();
        pnlComment = new javax.swing.JPanel();
        lblHint = new javax.swing.JLabel();
        lblComment = new javax.swing.JLabel();
        cmdExportResult = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(400, 480));
        setLayout(new java.awt.BorderLayout(0, 5));

        tblQueryResult.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Results"
            }
        ));
        scrQueryResult.setViewportView(tblQueryResult);

        add(scrQueryResult, java.awt.BorderLayout.CENTER);

        pnlCommandButton.setMinimumSize(new java.awt.Dimension(500, 32));
        pnlCommandButton.setPreferredSize(new java.awt.Dimension(500, 32));
        pnlCommandButton.setLayout(new java.awt.BorderLayout(0, 5));

        pnlComment.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 7, 5));

        lblHint.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblHint.setText("Hint:");
        pnlComment.add(lblHint);

        lblComment.setText("--");
        pnlComment.add(lblComment);

        pnlCommandButton.add(pnlComment, java.awt.BorderLayout.CENTER);

        cmdExportResult.setIcon(IconLoader.getImageIcon("images/export.png"));
        cmdExportResult.setText("Export to CSV...");
        cmdExportResult.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cmdExportResult.setContentAreaFilled(false);
        cmdExportResult.setFocusable(false);
        cmdExportResult.setIconTextGap(5);
        cmdExportResult.setMaximumSize(new java.awt.Dimension(125, 25));
        cmdExportResult.setMinimumSize(new java.awt.Dimension(125, 25));
        cmdExportResult.setPreferredSize(new java.awt.Dimension(125, 20));
        cmdExportResult.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdExportResultActionPerformed(evt);
            }
        });
        pnlCommandButton.add(cmdExportResult, java.awt.BorderLayout.EAST);

        add(pnlCommandButton, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents


	private void cmdExportResultActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_buttonSaveResultsActionPerformed
		JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
		fileChooser.setDialogTitle("Export to...");
		int response = fileChooser.showSaveDialog(this);
		if (response == JFileChooser.APPROVE_OPTION) {
			File targetFile = fileChooser.getSelectedFile();
			final String fileLocation = targetFile.getPath();
			if (canWrite(targetFile)) {
				Thread thread = new Thread() {
					public void run() {						
						getOBDASaveQueryToFileAction().run(fileLocation);
					}
				};
				thread.start();
			}
		}
	}// GEN-LAST:event_buttonSaveResultsActionPerformed

	/**
	 * A utility method to check if the result should be written to the target file. 
	 * Return true if the target file doesn't exist yet or the user allows overwriting.
	 */
	private boolean canWrite(File outputFile) {
		boolean fileIsValid = false;
		if (outputFile.exists()) {
			int result = JOptionPane.showConfirmDialog(
					this, "File exists, overwrite?",
					"Warning", JOptionPane.YES_NO_CANCEL_OPTION);
			switch (result) {
			case JOptionPane.YES_OPTION:
				fileIsValid = true;
				break;
			default:
				fileIsValid = false;
			}
		} else {
			fileIsValid = true;
		}
		return fileIsValid;
	}
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdExportResult;
    private javax.swing.JLabel lblComment;
    private javax.swing.JLabel lblHint;
    private javax.swing.JPanel pnlCommandButton;
    private javax.swing.JPanel pnlComment;
    private javax.swing.JScrollPane scrQueryResult;
    private javax.swing.JTable tblQueryResult;
    // End of variables declaration//GEN-END:variables
	
	public void setTableModel(final TableModel newmodel) {
		Runnable updateModel = new Runnable() {
			@Override
			public void run() {
				tblQueryResult.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
				ToolTipManager.sharedInstance().unregisterComponent(tblQueryResult);
				ToolTipManager.sharedInstance().unregisterComponent(tblQueryResult.getTableHeader());

				TableModel oldmodel = tblQueryResult.getModel();
				if (oldmodel != null) {
					oldmodel.removeTableModelListener(tblQueryResult);
					if (oldmodel instanceof IncrementalResultSetTableModel) {
						IncrementalResultSetTableModel incm = (IncrementalResultSetTableModel) oldmodel;
						incm.close();
					}
				}
				tblQueryResult.setModel(newmodel);

				addNotify();

				tblQueryResult.invalidate();
				tblQueryResult.repaint();
			}
		};
		SwingUtilities.invokeLater(updateModel);
		
		// Write a hint in the comment panel for user information
		writeHintMessage();
	}
	
	// TODO Change the implementation of checking the table model after refactoring the code.
	private void writeHintMessage() {
		String msg = "--";
        if (querypanel.isFetchAllSelect() || querypanel.canGetMoreTuples()) {
			msg = "Try to continue scrolling down the table to retrieve more results.";
		}
		lblComment.setText(msg);
	}

	private void addPopUpMenu(){
		JPopupMenu menu = new JPopupMenu();
		JMenuItem countAll = new JMenuItem(); 
		countAll.setText("count all tuples");
		countAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						String query = querypanel.getQuery();
						getCountAllTuplesActionForUCQ().run(query);
					}
				};
				thread.start();
			}
		});
		menu.add(countAll);
		tblQueryResult.setComponentPopupMenu(menu);
	}

	public OBDADataQueryAction getCountAllTuplesActionForUCQ() {
		return countAllTuplesAction;
	}

	public void setCountAllTuplesActionForUCQ(OBDADataQueryAction countAllTuples) {
		this.countAllTuplesAction = countAllTuples;
	}
	
	public OBDADataQueryAction getCountAllTuplesActionForEQL() {
		return countAllTuplesActionEQL;
	}

	public void setCountAllTuplesActionForEQL(OBDADataQueryAction countAllTuples) {
		this.countAllTuplesActionEQL = countAllTuples;
	}
	
	public void setOBDASaveQueryToFileAction(OBDASaveQueryResultToFileAction action){
		this.saveToFileAction = action;
	}
	
	public OBDASaveQueryResultToFileAction getOBDASaveQueryToFileAction(){
		return saveToFileAction;
	}
}
