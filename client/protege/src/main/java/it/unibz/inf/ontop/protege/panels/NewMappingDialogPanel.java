package it.unibz.inf.ontop.protege.panels;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.protege.core.*;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.protege.utils.IconLoader;
import it.unibz.inf.ontop.protege.gui.models.ResultSetTableModel;
import it.unibz.inf.ontop.protege.utils.*;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.protege.utils.DialogUtils.HTML_TAB;

public class NewMappingDialogPanel extends javax.swing.JPanel {

	private static final long serialVersionUID = 4351696247473906680L;

	private final OBDAModelManager obdaModelManager;
	private final JDialog parent;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private SQLPPTriplesMap mapping;
	private final TargetQueryStyledDocument targetDocument;

	private final Border defaultBorder;
	private final Border errorBorder;

	private static final int DEFAULT_TOOL_TIP_INITIAL_DELAY = ToolTipManager.sharedInstance().getInitialDelay();
	private static final int DEFAULT_TOOL_TIP_DISMISS_DELAY = ToolTipManager.sharedInstance().getDismissDelay();
	private static final int ERROR_TOOL_TIP_INITIAL_DELAY = 100;
	private static final int ERROR_TOOL_TIP_DISMISS_DELAY = 9000;


	/**
	 * Create the dialog for inserting a new mapping.
	 */
	public NewMappingDialogPanel(OBDAModelManager obdaModelManager, JDialog parent) {
		this.obdaModelManager = obdaModelManager;
		this.parent = parent;

		DialogUtils.installEscapeCloseOperation(parent);

		initComponents();

		defaultBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY);
		errorBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED);

		// Formatting the src query
		StyledDocument doc = txtSourceQuery.getStyledDocument();
		Style plainStyle = doc.addStyle("PLAIN_STYLE", null);
		StyleConstants.setItalic(plainStyle, false);
		StyleConstants.setSpaceAbove(plainStyle, 0);
		StyleConstants.setFontSize(plainStyle, 12);
		StyleConstants.setFontFamily(plainStyle, new Font("Dialog", Font.PLAIN, 12).getFamily());
		doc.setParagraphAttributes(0, doc.getLength(), plainStyle, true);

		txtMappingID.setFont(new Font("Dialog", Font.BOLD, 12));

		txtSourceQuery.setDocument(new SQLQueryStyledDocument());

		Timer timer = new Timer(1000, e -> targetValidation());
		timer.setRepeats(false);
		targetDocument = new TargetQueryStyledDocument(obdaModelManager, d -> timer.restart());
		txtTargetQuery.setDocument(targetDocument);

		cmdInsertMapping.setEnabled(false);
		cmdInsertMapping.addActionListener(this::cmdInsertMappingActionPerformed);

		txtTargetQuery.addKeyListener(new TABKeyListener());
		txtSourceQuery.addKeyListener(new TABKeyListener());
		tblQueryResult.setFocusable(false);

		txtTargetQuery.addKeyListener(new CTRLEnterKeyListener());
		txtSourceQuery.addKeyListener(new CTRLEnterKeyListener());
		txtMappingID.addKeyListener(new CTRLEnterKeyListener());

		cmdTestQuery.setFocusable(true);

		setFocusTraversalPolicy(new CustomTraversalPolicy(ImmutableList.of(
				txtMappingID,
				txtTargetQuery,
				txtSourceQuery,
				cmdTestQuery,
				cmdInsertMapping,
				cmdCancel)));
	}

	private void targetValidation() {
		try {
			ImmutableSet<IRI> iris = targetDocument.validate();
			if (iris.isEmpty())
				clearError();
			else {
				MutablePrefixManager prefixManager = obdaModelManager.getActiveOBDAModel().getMutablePrefixManager();
				setError("The following predicates are not declared in the ontology:\n "
						+ iris.stream()
						.map(IRI::getIRIString)
						.map(prefixManager::getShortForm)
						.map(iri -> "\t- " + iri)
						.collect(Collectors.joining(",\n")) + ".");
			}
		}
		catch (TargetQueryParserException e) {
			setError((e.getMessage() == null)
					? "Syntax error, check log"
					: e.getMessage().replace("'<EOF>'", "the end"));
		}
	}

	private void setError(String tooltip) {
		txtTargetQuery.setBorder(BorderFactory.createCompoundBorder(null, errorBorder));
		ToolTipManager.sharedInstance().setInitialDelay(ERROR_TOOL_TIP_INITIAL_DELAY);
		ToolTipManager.sharedInstance().setDismissDelay(ERROR_TOOL_TIP_DISMISS_DELAY);
		txtTargetQuery.setToolTipText("<html><body>" +
				tooltip.replace("<", "&lt;")
						.replace(">", "&gt;")
						.replace("\n", "<br>")
						.replace("\t", HTML_TAB)
				+ "</body></html>");
		cmdInsertMapping.setEnabled(false);
	}

	private void clearError() {
		cmdInsertMapping.setEnabled(true);
		txtTargetQuery.setToolTipText(null);
		txtTargetQuery.setBorder(BorderFactory.createCompoundBorder(null, defaultBorder));
		ToolTipManager.sharedInstance().setInitialDelay(DEFAULT_TOOL_TIP_INITIAL_DELAY);
		ToolTipManager.sharedInstance().setDismissDelay(DEFAULT_TOOL_TIP_DISMISS_DELAY);
	}


	private static class TABKeyListener extends KeyAdapter {
		@Override public void keyTyped(KeyEvent e) { typedOrPressed(e); }
		@Override public void keyPressed(KeyEvent e) { typedOrPressed(e); }

		private void typedOrPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_TAB) {
				if (e.getModifiers() == KeyEvent.SHIFT_MASK) {
					e.getComponent().transferFocusBackward();
				}
				else {
					e.getComponent().transferFocus();
				}
				e.consume();
			}
		}
	}

	private class CTRLEnterKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if (cmdInsertMapping.isEnabled() && (e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() == KeyEvent.VK_ENTER)) {
				cmdInsertMappingActionPerformed(null);
			}
			else if ((e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() == KeyEvent.VK_T)) {
				cmdTestQueryActionPerformed(null);
			}
		}
	}

	public void setID(String id) {
		this.txtMappingID.setText(id);
	}

	/***
	 * Sets the current mapping to the input. Note, if the current mapping is
	 * set, this means that this dialog is "updating" a mapping, and not
	 * creating a new one.
	 */
	public void setMapping(SQLPPTriplesMap mapping) {
		this.mapping = mapping;

		cmdInsertMapping.setText("Update");
		txtMappingID.setText(mapping.getId());

		SQLPPSourceQuery sourceQuery = mapping.getSourceQuery();
		String srcQuery = sourceQuery.getSQL();
		txtSourceQuery.setText(srcQuery);

		String trgQuery = obdaModelManager.getActiveOBDAModel().getTargetRendering(mapping);
		txtTargetQuery.setText(trgQuery);
		targetValidation();
	}


	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblMappingID = new javax.swing.JLabel();
        pnlTestButton = new javax.swing.JPanel();
        cmdTestQuery = new javax.swing.JButton();
        lblTestQuery = new javax.swing.JLabel();
        pnlCommandButton = new javax.swing.JPanel();
        cmdInsertMapping = new javax.swing.JButton();
        cmdCancel = new javax.swing.JButton();
        txtMappingID = new javax.swing.JTextField();
        splitTargetSource = new javax.swing.JSplitPane();
        pnlTargetQueryEditor = new javax.swing.JPanel();
        lblTargetQuery = new javax.swing.JLabel();
        scrTargetQuery = new javax.swing.JScrollPane();
        txtTargetQuery = new javax.swing.JTextPane();
        splitSQL = new javax.swing.JSplitPane();
        pnlSourceQueryEditor = new javax.swing.JPanel();
        lblSourceQuery = new javax.swing.JLabel();
        scrSourceQuery = new javax.swing.JScrollPane();
        txtSourceQuery = new javax.swing.JTextPane();
        pnlQueryResult = new javax.swing.JPanel();
        scrQueryResult = new javax.swing.JScrollPane();
        tblQueryResult = new javax.swing.JTable();

        setFocusable(false);
        setMinimumSize(new java.awt.Dimension(600, 500));
        setPreferredSize(new java.awt.Dimension(600, 500));
        setLayout(new java.awt.GridBagLayout());

        lblMappingID.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblMappingID.setText("Mapping ID:");
        lblMappingID.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 10, 8, 0);
        add(lblMappingID, gridBagConstraints);

        cmdTestQuery.setIcon(IconLoader.getImageIcon("images/execute.png"));
        cmdTestQuery.setMnemonic('t');
        cmdTestQuery.setText("Test SQL Query");
        cmdTestQuery.setToolTipText("Execute the SQL query in the SQL query text pane\nand display the first 100 results in the table.");
        cmdTestQuery.setActionCommand("Test SQL query");
        cmdTestQuery.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cmdTestQuery.setContentAreaFilled(false);
        cmdTestQuery.setIconTextGap(5);
        cmdTestQuery.setMaximumSize(new java.awt.Dimension(115, 25));
        cmdTestQuery.setMinimumSize(new java.awt.Dimension(115, 25));
        cmdTestQuery.setPreferredSize(new java.awt.Dimension(115, 25));
        cmdTestQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdTestQueryActionPerformed(evt);
            }
        });
        pnlTestButton.add(cmdTestQuery);

        lblTestQuery.setText("(100 rows)");
        pnlTestButton.add(lblTestQuery);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 0);
        add(pnlTestButton, gridBagConstraints);

        pnlCommandButton.setFocusable(false);
        pnlCommandButton.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        cmdInsertMapping.setIcon(IconLoader.getImageIcon("images/accept.png"));
        cmdInsertMapping.setText("Accept");
        cmdInsertMapping.setToolTipText("This will add/edit the current mapping into the OBDA model");
        cmdInsertMapping.setActionCommand("OK");
        cmdInsertMapping.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cmdInsertMapping.setContentAreaFilled(false);
        cmdInsertMapping.setIconTextGap(5);
        cmdInsertMapping.setPreferredSize(new java.awt.Dimension(90, 25));
        pnlCommandButton.add(cmdInsertMapping);

        cmdCancel.setIcon(IconLoader.getImageIcon("images/cancel.png"));
        cmdCancel.setText("Cancel");
        cmdCancel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        cmdCancel.setContentAreaFilled(false);
        cmdCancel.setIconTextGap(5);
        cmdCancel.setPreferredSize(new java.awt.Dimension(90, 25));
        cmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCancelActionPerformed(evt);
            }
        });
        pnlCommandButton.add(cmdCancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 4);
        add(pnlCommandButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 10);
        add(txtMappingID, gridBagConstraints);

        splitTargetSource.setBorder(null);
        splitTargetSource.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitTargetSource.setResizeWeight(0.5);
        splitTargetSource.setDoubleBuffered(true);
        splitTargetSource.setFocusable(false);
        splitTargetSource.setMinimumSize(new java.awt.Dimension(600, 430));
        splitTargetSource.setOneTouchExpandable(true);
        splitTargetSource.setPreferredSize(new java.awt.Dimension(600, 430));

        pnlTargetQueryEditor.setFocusable(false);
        pnlTargetQueryEditor.setMinimumSize(new java.awt.Dimension(600, 180));
        pnlTargetQueryEditor.setPreferredSize(new java.awt.Dimension(600, 180));
        pnlTargetQueryEditor.setLayout(new java.awt.BorderLayout());

        lblTargetQuery.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblTargetQuery.setText("Target (Triples Template):");
        lblTargetQuery.setFocusable(false);
        pnlTargetQueryEditor.add(lblTargetQuery, java.awt.BorderLayout.NORTH);

        scrTargetQuery.setFocusable(false);
        scrTargetQuery.setMinimumSize(new java.awt.Dimension(600, 170));
        scrTargetQuery.setPreferredSize(new java.awt.Dimension(600, 170));

        txtTargetQuery.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 13)); // NOI18N
        txtTargetQuery.setFocusCycleRoot(false);
        txtTargetQuery.setMinimumSize(new java.awt.Dimension(600, 170));
        txtTargetQuery.setPreferredSize(new java.awt.Dimension(600, 170));
        scrTargetQuery.setViewportView(txtTargetQuery);

        pnlTargetQueryEditor.add(scrTargetQuery, java.awt.BorderLayout.CENTER);

        splitTargetSource.setLeftComponent(pnlTargetQueryEditor);

        splitSQL.setBorder(null);
        splitSQL.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitSQL.setResizeWeight(0.8);
        splitSQL.setFocusable(false);
        splitSQL.setMinimumSize(new java.awt.Dimension(600, 280));
        splitSQL.setOneTouchExpandable(true);
        splitSQL.setPreferredSize(new java.awt.Dimension(600, 280));

        pnlSourceQueryEditor.setFocusable(false);
        pnlSourceQueryEditor.setMinimumSize(new java.awt.Dimension(600, 150));
        pnlSourceQueryEditor.setPreferredSize(new java.awt.Dimension(600, 150));
        pnlSourceQueryEditor.setLayout(new java.awt.BorderLayout());

        lblSourceQuery.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblSourceQuery.setText("Source (SQL Query):");
        lblSourceQuery.setFocusable(false);
        pnlSourceQueryEditor.add(lblSourceQuery, java.awt.BorderLayout.NORTH);

        scrSourceQuery.setFocusable(false);

        txtSourceQuery.setFont(new java.awt.Font("Lucida Sans Typewriter", 0, 13)); // NOI18N
        txtSourceQuery.setFocusCycleRoot(false);
        scrSourceQuery.setViewportView(txtSourceQuery);

        pnlSourceQueryEditor.add(scrSourceQuery, java.awt.BorderLayout.CENTER);

        splitSQL.setTopComponent(pnlSourceQueryEditor);

        pnlQueryResult.setFocusable(false);
        pnlQueryResult.setMinimumSize(new java.awt.Dimension(600, 120));
        pnlQueryResult.setPreferredSize(new java.awt.Dimension(600, 120));
        pnlQueryResult.setLayout(new java.awt.BorderLayout());

        scrQueryResult.setFocusable(false);
        scrQueryResult.setPreferredSize(new java.awt.Dimension(454, 70));

        tblQueryResult.setMinimumSize(new java.awt.Dimension(600, 180));
        scrQueryResult.setViewportView(tblQueryResult);

        pnlQueryResult.add(scrQueryResult, java.awt.BorderLayout.CENTER);

        splitSQL.setBottomComponent(pnlQueryResult);

        splitTargetSource.setRightComponent(splitSQL);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(splitTargetSource, gridBagConstraints);

        getAccessibleContext().setAccessibleName("Mapping editor");
    }// </editor-fold>//GEN-END:initComponents

	private void cmdTestQueryActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonTestActionPerformed
		OBDAProgressMonitor progMonitor = new OBDAProgressMonitor("Executing query...", this);
		CountDownLatch latch = new CountDownLatch(1);
		ExecuteSQLQueryAction action = new ExecuteSQLQueryAction(latch);
		progMonitor.addProgressListener(action);
		progMonitor.start();
		try {
			action.run();
			latch.await();
			progMonitor.stop();
			try (ResultSet set = action.result) {
				if (set != null) {
					ResultSetTableModel model = new ResultSetTableModel(set);
					tblQueryResult.setModel(model);
					scrQueryResult.getParent().revalidate();
				}
			}
			finally {
				if (action.statement != null && !action.statement.isClosed()) {
					action.statement.close();
				}
			}
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}
	}// GEN-LAST:event_jButtonTestActionPerformed

	private class ExecuteSQLQueryAction implements OBDAProgressListener {

		private final CountDownLatch latch;
		private Thread thread = null;
		private ResultSet result;
		private Statement statement = null;
		private boolean isCancelled = false;
		private boolean errorShown = false;

		private ExecuteSQLQueryAction(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void actionCanceled() throws SQLException {
			this.isCancelled = true;
			if (thread != null) {
				thread.interrupt();
			}
			if (statement != null && !statement.isClosed()) {
				statement.close();
			}
			result = null;
			latch.countDown();
		}

		public void run() {
			thread = new Thread(() -> {
				try {
					JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();
					OBDADataSource dataSource = obdaModelManager.getDatasource();
					Connection c = man.getConnection(dataSource.getURL(), dataSource.getUsername(), dataSource.getPassword());

					statement = c.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
					statement.setMaxRows(100);
					result = statement.executeQuery(txtSourceQuery.getText().trim());
					latch.countDown();
				}
				catch (Exception e) {
					latch.countDown();
					errorShown = true;
					DialogUtils.showSeeLogErrorDialog(getRootPane(), "", log, e);
				}
			});
			thread.start();
		}

		@Override
		public boolean isCancelled() {
			return this.isCancelled;
		}

		@Override
		public boolean isErrorShown() {
			return this.errorShown;
		}
	}

	private void cmdInsertMappingActionPerformed(ActionEvent e) {// GEN-FIRST:event_cmdInsertMappingActionPerformed
		String newId = txtMappingID.getText().trim();
		if (newId.isEmpty()) {
			JOptionPane.showMessageDialog(this, "The ID cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String target = txtTargetQuery.getText();
		if (target.isEmpty()) {
			JOptionPane.showMessageDialog(this, "The target cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String source = txtSourceQuery.getText().trim();
		if (source.isEmpty()) {
			JOptionPane.showMessageDialog(this, "The source cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		OBDAModel obdaModel = obdaModelManager.getActiveOBDAModel();
		try {
			ImmutableList<TargetAtom> targetQuery = obdaModel.parseTargetQuery(target);

			try {
				log.info("Insert Mapping: \n"+ target + "\n" + source);

				if (mapping == null) {
					obdaModel.add(newId, source, targetQuery);
				}
				else {
					obdaModel.update(mapping.getId(), newId, source, targetQuery);
				}
			}
			catch (DuplicateMappingException e1) {
				JOptionPane.showMessageDialog(this, "Error while inserting mapping: " + e1.getMessage() + " is already taken");
				return;
			}
			parent.setVisible(false);
			parent.dispose();
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(this, "Error while inserting mapping: " + e1.getMessage());
		}
	}// GEN-LAST:event_cmdInsertMappingActionPerformed

	private void cmdCancelActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdCancelActionPerformed
		parent.setVisible(false);
		parent.dispose();
	}// GEN-LAST:event_cmdCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cmdCancel;
    private javax.swing.JButton cmdInsertMapping;
    private javax.swing.JButton cmdTestQuery;
    private javax.swing.JLabel lblMappingID;
    private javax.swing.JLabel lblSourceQuery;
    private javax.swing.JLabel lblTargetQuery;
    private javax.swing.JLabel lblTestQuery;
    private javax.swing.JPanel pnlCommandButton;
    private javax.swing.JPanel pnlQueryResult;
    private javax.swing.JPanel pnlSourceQueryEditor;
    private javax.swing.JPanel pnlTargetQueryEditor;
    private javax.swing.JPanel pnlTestButton;
    private javax.swing.JScrollPane scrQueryResult;
    private javax.swing.JScrollPane scrSourceQuery;
    private javax.swing.JScrollPane scrTargetQuery;
    private javax.swing.JSplitPane splitSQL;
    private javax.swing.JSplitPane splitTargetSource;
    private javax.swing.JTable tblQueryResult;
    private javax.swing.JTextField txtMappingID;
    private javax.swing.JTextPane txtSourceQuery;
    private javax.swing.JTextPane txtTargetQuery;
    // End of variables declaration//GEN-END:variables


}
