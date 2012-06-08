/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, 
 * b) third-party components licensed under terms that may be different from 
 *   those of the LGPL.  Information about such licenses can be found in the 
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package it.unibz.krdb.obda.gui.swing.panel;

import it.unibz.krdb.obda.gui.swing.IconLoader;
import it.unibz.krdb.obda.gui.swing.OBDADataQueryAction;
import it.unibz.krdb.obda.gui.swing.OBDASaveQueryResultToFileAction;
import it.unibz.krdb.obda.gui.swing.treemodel.IncrementalResultSetTableModel;

import java.awt.EventQueue;
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
	// <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrQueryResult = new javax.swing.JScrollPane();
        tblQueryResult = new javax.swing.JTable();
        pnlCommandButton = new javax.swing.JPanel();
        cmdExportResult = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(400, 480));
        setLayout(new java.awt.BorderLayout());

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
        pnlCommandButton.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        cmdExportResult.setIcon(IconLoader.getImageIcon("images/export.png"));
        cmdExportResult.setText("Export to CSV...");
        cmdExportResult.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cmdExportResult.setContentAreaFilled(false);
        cmdExportResult.setFocusable(false);
        cmdExportResult.setIconTextGap(5);
        cmdExportResult.setMaximumSize(new java.awt.Dimension(125, 25));
        cmdExportResult.setMinimumSize(new java.awt.Dimension(125, 25));
        cmdExportResult.setPreferredSize(new java.awt.Dimension(125, 25));
        cmdExportResult.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdExportResultActionPerformed(evt);
            }
        });
        pnlCommandButton.add(cmdExportResult);

        add(pnlCommandButton, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents


	private void cmdExportResultActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_buttonSaveResultsActionPerformed
		JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
		fileChooser.setDialogTitle("Export to...");
		int response = fileChooser.showSaveDialog(this);
		if (response == JFileChooser.APPROVE_OPTION) {
			File targetFile = fileChooser.getSelectedFile();
			if (canWrite(targetFile)) {
				final String fileLocation = targetFile.getPath();
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						saveToFileAction.run(fileLocation);
					}
				});
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
    private javax.swing.JPanel pnlCommandButton;
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
						getCountAllTuplesActionForUCQ().run(query, querypanel);
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
}
