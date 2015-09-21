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

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.TableModel;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.io.TargetQueryVocabularyValidator;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.parser.TargetQueryParserException;
import org.semanticweb.ontop.parser.TurtleOBDASyntaxParser;
import org.semanticweb.ontop.protege4.core.OBDAModelWrapper;
import org.semanticweb.ontop.protege4.gui.IconLoader;
import org.semanticweb.ontop.protege4.gui.treemodels.IncrementalResultSetTableModel;
import org.semanticweb.ontop.protege4.utils.CustomTraversalPolicy;
import org.semanticweb.ontop.protege4.utils.DatasourceSelectorListener;
import org.semanticweb.ontop.protege4.utils.DialogUtils;
import org.semanticweb.ontop.protege4.utils.OBDAProgessMonitor;
import org.semanticweb.ontop.protege4.utils.OBDAProgressListener;
import org.semanticweb.ontop.protege4.utils.QueryPainter;
import org.semanticweb.ontop.protege4.utils.QueryPainter.ValidatorListener;
import org.semanticweb.ontop.renderer.SourceQueryRenderer;
import org.semanticweb.ontop.renderer.TargetQueryRenderer;
import org.semanticweb.ontop.sql.JDBCConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewMappingDialogPanel extends javax.swing.JPanel implements DatasourceSelectorListener {

	private static final long serialVersionUID = 4351696247473906680L;

	/** Fields */
	private OBDAModelWrapper obdaModel;
	private OBDADataSource dataSource;
	private JDialog parent;
	private TargetQueryVocabularyValidator validator;
	private OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();

	private PrefixManager prefixManager;
	
	/** Logger */
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Create the dialog for inserting a new mapping.
	 */
	public NewMappingDialogPanel(OBDAModelWrapper obdaModel, JDialog parent, OBDADataSource dataSource, TargetQueryVocabularyValidator validator) {

		DialogUtils.installEscapeCloseOperation(parent);
		this.obdaModel = obdaModel;
		this.parent = parent;
		this.dataSource = dataSource;
		this.validator = validator;

		prefixManager = obdaModel.getPrefixManager();
		
		initComponents();

		// Formatting the src query
		StyledDocument doc = txtSourceQuery.getStyledDocument();
		Style plainStyle = doc.addStyle("PLAIN_STYLE", null);
		StyleConstants.setItalic(plainStyle, false);
		StyleConstants.setSpaceAbove(plainStyle, 0);
		StyleConstants.setFontSize(plainStyle, 12);
		StyleConstants.setFontFamily(plainStyle, new Font("Dialog", Font.PLAIN, 12).getFamily());
		doc.setParagraphAttributes(0, doc.getLength(), plainStyle, true);

		txtMappingID.setFont(new Font("Dialog", Font.BOLD, 12));

		cmdInsertMapping.setEnabled(false);
		QueryPainter painter = new QueryPainter(obdaModel, txtTargetQuery, validator);
		painter.addValidatorListener(new ValidatorListener() {

			@Override
			public void validated(boolean result) {
				cmdInsertMapping.setEnabled(result);
			}
		});

		cmdInsertMapping.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdInsertMappingActionPerformed(e);
			}
		});

		txtTargetQuery.addKeyListener(new TABKeyListener());
		txtSourceQuery.addKeyListener(new TABKeyListener());
		tblQueryResult.setFocusable(false);

		txtTargetQuery.addKeyListener(new CTRLEnterKeyListener());
		txtSourceQuery.addKeyListener(new CTRLEnterKeyListener());
		txtMappingID.addKeyListener(new CTRLEnterKeyListener());

		cmdTestQuery.setFocusable(true);
		Vector<Component> order = new Vector<Component>(7);
		order.add(this.txtMappingID);
		order.add(this.txtTargetQuery);
		order.add(this.txtSourceQuery);
		order.add(this.cmdTestQuery);
		order.add(this.cmdInsertMapping);
		order.add(this.cmdCancel);
		this.setFocusTraversalPolicy(new CustomTraversalPolicy(order));
	}

	private class TABKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_TAB) {
				if (e.getModifiers() == KeyEvent.SHIFT_MASK) {
					e.getComponent().transferFocusBackward();
				} else {
					e.getComponent().transferFocus();
				}
				e.consume();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// if (e.getKeyCode() == KeyEvent.VK_TAB) {
			// e.getComponent().transferFocus();
			// e.consume();
			// }
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_TAB) {
				if (e.getModifiers() == KeyEvent.SHIFT_MASK) {
					e.getComponent().transferFocusBackward();
				} else {
					e.getComponent().transferFocus();
				}
				e.consume();
			}
		}
	}

	private class CTRLEnterKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
			// NO-OP
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (cmdInsertMapping.isEnabled() && (e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() == KeyEvent.VK_ENTER)) {
				cmdInsertMappingActionPerformed(null);
			} else if ((e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() == KeyEvent.VK_T)) {
				cmdTestQueryActionPerformed(null);
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}

	private void insertMapping(String target, String source) {
		CQIE targetQuery = parse(target);
		if (targetQuery != null) {
			final boolean isValid = validator.validate(targetQuery);
			if (isValid) {
				try {
					OBDAModelWrapper mapcon = obdaModel;
					URI sourceID = dataSource.getSourceID();
					System.out.println(sourceID.toString()+" \n");
					
					OBDASQLQuery body = dataFactory.getSQLQuery(source);
					System.out.println(body.toString()+" \n");

					OBDAMappingAxiom newmapping = dataFactory.getMappingAxiom(txtMappingID.getText().trim(), body, targetQuery);
					System.out.println(newmapping.toString()+" \n");

					if (mapping == null) {
						// Case when we are creating a new mapping
						mapcon.addMapping(sourceID, newmapping);
					} else {
						// Case when we are updating an existing mapping
						mapcon.updateMappingsSourceQuery(sourceID, mapping.getId(), body);
						mapcon.updateTargetQueryMapping(sourceID, mapping.getId(), targetQuery);
						mapcon.updateMapping(sourceID, mapping.getId(), txtMappingID.getText().trim());
					}
				} catch (DuplicateMappingException e) {
					JOptionPane.showMessageDialog(this, "Error while inserting mapping: " + e.getMessage() + " is already taken");
					return;
				}
				parent.setVisible(false);
				parent.dispose();
				
			} else {
				// List of invalid predicates that are found by the validator.
				Vector<String> invalidPredicates = validator.getInvalidPredicates();
				String invalidList = "";
				for (String predicate : invalidPredicates) {
					invalidList += "- " + predicate + "\n";
				}
				JOptionPane.showMessageDialog(this, "This list of predicates is unknown by the ontology: \n" + invalidList, "New Mapping", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		lblMappingID = new javax.swing.JLabel();
		cmdTestQuery = new javax.swing.JButton();
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
		cmdTestQuery.setToolTipText("Execute the SQL query in the SQL query text pane<p> and display the results in the table bellow.");
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
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.insets = new java.awt.Insets(4, 10, 4, 4);
		add(cmdTestQuery, gridBagConstraints);

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

		tblQueryResult.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

		}, new String[] {

		}));
		tblQueryResult.setMinimumSize(new java.awt.Dimension(600, 180));
		tblQueryResult.setPreferredSize(new java.awt.Dimension(600, 180));
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

	private void releaseResultset() {
		TableModel model = tblQueryResult.getModel();
		if (model == null)
			return;
		if (!(model instanceof IncrementalResultSetTableModel))
			return;
		IncrementalResultSetTableModel imodel = (IncrementalResultSetTableModel) model;
		imodel.close();
	}

	private void cmdTestQueryActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonTestActionPerformed
		// Cleaning the existing table and releasing resources
		releaseResultset();

		OBDAProgessMonitor progMonitor = new OBDAProgessMonitor("Executing query...");
		CountDownLatch latch = new CountDownLatch(1);
		ExecuteSQLQueryAction action = new ExecuteSQLQueryAction(latch);
		progMonitor.addProgressListener(action);
		progMonitor.start();
		try {
			action.run();
			latch.await();
			progMonitor.stop();
			ResultSet set = action.getResult();
			if (set != null) {
				IncrementalResultSetTableModel model = new IncrementalResultSetTableModel(set);
				tblQueryResult.setModel(model);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}// GEN-LAST:event_jButtonTestActionPerformed

	private class ExecuteSQLQueryAction implements OBDAProgressListener {

		CountDownLatch latch = null;
		Thread thread = null;
		ResultSet result = null;
		Statement statement = null;
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

		public ResultSet getResult() {
			return result;
		}

		public void run() {
			thread = new Thread() {
				public void run() {
					try {
						TableModel oldmodel = tblQueryResult.getModel();

						if ((oldmodel != null) && (oldmodel instanceof IncrementalResultSetTableModel)) {
							IncrementalResultSetTableModel rstm = (IncrementalResultSetTableModel) oldmodel;
							rstm.close();
						}
						JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();

						Connection c = man.getConnection(dataSource);

//						String driver = dataSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

						Statement st = c.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
						// if (driver.equals("com.mysql.jdbc.Driver")) {
						// st.setFetchSize(Integer.MIN_VALUE);
						// }

						result = st.executeQuery(txtSourceQuery.getText().trim());
						latch.countDown();
					} catch (Exception e) {
						latch.countDown();
						DialogUtils.showQuickErrorDialog(null, e);
					    errorShown = true;
					}
				}
			};
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

		releaseResultset();

		final String targetQueryString = txtTargetQuery.getText();
		final String sourceQueryString = txtSourceQuery.getText();

		if (txtMappingID.getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "ERROR: The ID cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (targetQueryString.isEmpty()) {
			JOptionPane.showMessageDialog(this, "ERROR: The target cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (sourceQueryString.isEmpty()) {
			JOptionPane.showMessageDialog(this, "ERROR: The source cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		insertMapping(targetQueryString, sourceQueryString);
	}// GEN-LAST:event_cmdInsertMappingActionPerformed
	
	private void cmdCancelActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdCancelActionPerformed
		parent.setVisible(false);
		parent.dispose();
		releaseResultset();
	}// GEN-LAST:event_cmdCancelActionPerformed

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton cmdCancel;
	private javax.swing.JButton cmdInsertMapping;
	private javax.swing.JButton cmdTestQuery;
	private javax.swing.JLabel lblMappingID;
	private javax.swing.JLabel lblSourceQuery;
	private javax.swing.JLabel lblTargetQuery;
	private javax.swing.JPanel pnlCommandButton;
	private javax.swing.JPanel pnlQueryResult;
	private javax.swing.JPanel pnlSourceQueryEditor;
	private javax.swing.JPanel pnlTargetQueryEditor;
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

	private OBDAMappingAxiom mapping;

	private CQIE parse(String query) {
		TurtleOBDASyntaxParser textParser = new TurtleOBDASyntaxParser(obdaModel.getPrefixManager().getPrefixMap());
		try {
			return textParser.parse(query);
		} catch (TargetQueryParserException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void datasourceChanged(OBDADataSource oldSource, OBDADataSource newSource) {
		dataSource = newSource;
	}

	public void setID(String id) {
		this.txtMappingID.setText(id);
	}

	@Override
	public void finalize() {
		releaseResultset();
	}

	/***
	 * Sets the current mapping to the input. Note, if the current mapping is
	 * set, this means that this dialog is "updating" a mapping, and not
	 * creating a new one.
	 */
	public void setMapping(OBDAMappingAxiom mapping) {
		this.mapping = mapping;

		cmdInsertMapping.setText("Update");
		txtMappingID.setText(mapping.getId());

		OBDAQuery sourceQuery = mapping.getSourceQuery();
		String srcQuery = SourceQueryRenderer.encode(sourceQuery);
		txtSourceQuery.setText(srcQuery);

		OBDAQuery targetQuery = mapping.getTargetQuery();
		String trgQuery = TargetQueryRenderer.encode(targetQuery, prefixManager);
		txtTargetQuery.setText(trgQuery);
	}
}
