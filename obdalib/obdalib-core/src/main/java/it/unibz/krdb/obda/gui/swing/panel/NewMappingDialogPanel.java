package it.unibz.krdb.obda.gui.swing.panel;

import it.unibz.krdb.obda.codec.TargetQueryToTurtleCodec;
import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.gui.swing.treemodel.IncrementalResultSetTableModel;
import it.unibz.krdb.obda.gui.swing.treemodel.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.gui.swing.utils.DatasourceSelectorListener;
import it.unibz.krdb.obda.gui.swing.utils.DialogUtils;
import it.unibz.krdb.obda.gui.swing.utils.OBDAProgessMonitor;
import it.unibz.krdb.obda.gui.swing.utils.OBDAProgressListener;
import it.unibz.krdb.obda.gui.swing.utils.QueryPainter;
import it.unibz.krdb.obda.gui.swing.utils.QueryPainter.ValidatorListener;
import it.unibz.krdb.obda.gui.swing.utils.SQLQueryPainter;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDARDBMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.parser.TurtleSyntaxParser;
import it.unibz.krdb.obda.utils.OBDAPreferences;
import it.unibz.krdb.sql.JDBCConnectionManager;

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

import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewMappingDialogPanel extends javax.swing.JPanel implements DatasourceSelectorListener {

	private static final long serialVersionUID = 4351696247473906680L;

	/** Fields */
	private OBDAModel controller = null;
//	private OBDAPreferences preferences = null;
	private OBDADataSource dataSource = null;
	private JDialog parent = null;
	private TargetQueryVocabularyValidator validator = null;
	private OBDADataFactory dataFactory = OBDADataFactoryImpl.getInstance();

	/** Logger */
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Create the dialog for inserting a new mapping.
	 * 
	 * @param controller
	 * @param preference
	 * @param parent
	 * @param dataSource
	 * @param ontology
	 */
	public NewMappingDialogPanel(OBDAModel controller, JDialog parent, OBDADataSource dataSource,
			TargetQueryVocabularyValidator validator) {
		DialogUtils.installEscapeCloseOperation(parent);
		this.controller = controller;
//		this.preferences = preference;
		this.parent = parent;
		this.dataSource = dataSource;
		this.validator = validator;

		// validator = new TargetQueryValidator(ontology);

		initComponents();

		/***
		 * Formatting the src query
		 */
		StyledDocument doc = txtSourceQuery.getStyledDocument();
		Style plainStyle = doc.addStyle("PLAIN_STYLE", null);
		StyleConstants.setItalic(plainStyle, false);
		StyleConstants.setSpaceAbove(plainStyle, 0);
		StyleConstants.setFontSize(plainStyle, 12);
		StyleConstants.setFontFamily(plainStyle, new Font("Dialog", Font.PLAIN, 12).getFamily());
		doc.setParagraphAttributes(0, doc.getLength(), plainStyle, true);

		fieldID.setFont(new Font("Dialog", Font.BOLD, 12));

		cmdInsertMapping.setEnabled(false);
		QueryPainter painter = new QueryPainter(controller, txtTargetQuery, validator);
		painter.addValidatorListener(new ValidatorListener() {

			@Override
			public void validated(boolean result) {
				cmdInsertMapping.setEnabled(result);
			}
		});

		SQLQueryPainter sqlpainter = new SQLQueryPainter(txtSourceQuery);

		cmdInsertMapping.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cmdInsertMappingActionPerformed(e);

			}
		});

		txtTargetQuery.addKeyListener(new CTRLEnterKeyListener());
		txtSourceQuery.addKeyListener(new CTRLEnterKeyListener());
		fieldID.addKeyListener(new CTRLEnterKeyListener());

		// jPanel1.setPreferredSize(new Dimension(Integer.MAX_VALUE,
		// Integer.MAX_VALUE));
		// jPanel2.setMinimumSize(new Dimension(0, 0));
		// jPanel2.setPreferredSize(new Dimension(50,100));
		// jSplitPane1.setDividerLocation(0.5);

	}

	private class CTRLEnterKeyListener implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (cmdInsertMapping.isEnabled() && (e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() == KeyEvent.VK_ENTER)) {
				cmdInsertMappingActionPerformed(null);
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
					OBDAModel mapcon = controller;
					URI sourceID = dataSource.getSourceID();

					OBDASQLQuery body = dataFactory.getSQLQuery(source);
					OBDARDBMappingAxiom newmapping = dataFactory.getRDBMSMappingAxiom(fieldID.getText().trim(), body, targetQuery);

					if (mapping == null) {
						/***
						 * Case when we are creating a new mapping
						 */

						mapcon.addMapping(sourceID, newmapping);
					} else {
						/***
						 * Case when we are updating an existing mapping
						 */

						mapcon.updateMappingsSourceQuery(sourceID, mapping.getId(), body);
						mapcon.updateTargetQueryMapping(sourceID, mapping.getId(), targetQuery);
						mapcon.updateMapping(sourceID, mapping.getId(), fieldID.getText().trim());

					}
				} catch (DuplicateMappingException e) {
					JOptionPane.showMessageDialog(this, "Error while inserting mapping: " + e.getMessage() + " is already taken");
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
				JOptionPane.showMessageDialog(null, "This list of predicates is unknown by the ontology: \n" + invalidList, "New Mapping",
						JOptionPane.WARNING_MESSAGE);
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
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// desc=" Generated Code ">//GEN-BEGIN:initComponents
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		labelID = new javax.swing.JLabel();
		cmdTestQuery = new javax.swing.JButton();
		pnlCommandButton = new javax.swing.JPanel();
		cmdInsertMapping = new javax.swing.JButton();
		cmdCancel = new javax.swing.JButton();
		fieldID = new javax.swing.JTextField();
		splitTargetSource = new javax.swing.JSplitPane();
		panelTrg = new javax.swing.JPanel();
		lblTargetQuery = new javax.swing.JLabel();
		scrTargetQuery = new javax.swing.JScrollPane();
		txtTargetQuery = new javax.swing.JTextPane();
		splitSQL = new javax.swing.JSplitPane();
		jPanel1 = new javax.swing.JPanel();
		lblSourceQuery = new javax.swing.JLabel();
		scrSourceQuery = new javax.swing.JScrollPane();
		txtSourceQuery = new javax.swing.JTextPane();
		jPanel2 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jTable1 = new javax.swing.JTable();

		setLayout(new java.awt.GridBagLayout());

		setBorder(javax.swing.BorderFactory.createTitledBorder("Mapping Editor"));
		setFocusable(false);
		setMinimumSize(new java.awt.Dimension(600, 480));
		setPreferredSize(new java.awt.Dimension(400, 300));
		getAccessibleContext().setAccessibleName("Mapping editor");
		labelID.setText("Mapping ID:");
		labelID.setFocusable(false);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		add(labelID, gridBagConstraints);

		cmdTestQuery.setText("Test SQL Query");
		cmdTestQuery.setToolTipText("Execute the SQL query in the SQL query text pane<p> and display the results in the table bellow.");
		cmdTestQuery.setActionCommand("Test SQL query");
		cmdTestQuery.setNextFocusableComponent(cmdInsertMapping);
		cmdTestQuery.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cmdTestQueryActionPerformed(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
		add(cmdTestQuery, gridBagConstraints);

		pnlCommandButton.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

		pnlCommandButton.setFocusable(false);
		cmdInsertMapping.setText("Accept");
		cmdInsertMapping.setToolTipText("This will add/edit the current mapping into the OBDA model");
		cmdInsertMapping.setActionCommand("OK");
		cmdInsertMapping.setNextFocusableComponent(cmdCancel);
		pnlCommandButton.add(cmdInsertMapping);

		cmdCancel.setText("Cancel");
		cmdCancel.setNextFocusableComponent(fieldID);
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
		gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
		add(pnlCommandButton, gridBagConstraints);

		fieldID.setFocusCycleRoot(true);
		fieldID.setNextFocusableComponent(txtTargetQuery);
		fieldID.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fieldIDActionPerformed(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		add(fieldID, gridBagConstraints);

		splitTargetSource.setBorder(null);
		splitTargetSource.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		splitTargetSource.setResizeWeight(0.5);
		splitTargetSource.setDoubleBuffered(true);
		splitTargetSource.setFocusable(false);
		splitTargetSource.setOneTouchExpandable(true);
		panelTrg.setLayout(new java.awt.BorderLayout());

		panelTrg.setFocusable(false);
		panelTrg.setPreferredSize(new java.awt.Dimension(85, 200));
		lblTargetQuery.setText("Target Query:");
		lblTargetQuery.setFocusable(false);
		panelTrg.add(lblTargetQuery, java.awt.BorderLayout.NORTH);

		scrTargetQuery.setFocusable(false);
		txtTargetQuery.setFocusCycleRoot(false);
		txtTargetQuery.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				changeTargetQueryFocus(evt);
			}
		});

		scrTargetQuery.setViewportView(txtTargetQuery);

		panelTrg.add(scrTargetQuery, java.awt.BorderLayout.CENTER);

		splitTargetSource.setLeftComponent(panelTrg);

		splitSQL.setBorder(null);
		splitSQL.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		splitSQL.setResizeWeight(0.8);
		splitSQL.setFocusable(false);
		splitSQL.setOneTouchExpandable(true);
		jPanel1.setLayout(new java.awt.BorderLayout());

		jPanel1.setFocusable(false);
		lblSourceQuery.setText("Source Query:");
		lblSourceQuery.setFocusable(false);
		jPanel1.add(lblSourceQuery, java.awt.BorderLayout.NORTH);

		scrSourceQuery.setFocusable(false);
		txtSourceQuery.setFocusCycleRoot(false);
		txtSourceQuery.setNextFocusableComponent(cmdTestQuery);
		txtSourceQuery.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				changeSourceQueryFocus(evt);
			}
		});

		scrSourceQuery.setViewportView(txtSourceQuery);

		jPanel1.add(scrSourceQuery, java.awt.BorderLayout.CENTER);

		splitSQL.setTopComponent(jPanel1);

		jPanel2.setLayout(new java.awt.BorderLayout());

		jPanel2.setFocusable(false);
		jScrollPane1.setFocusable(false);
		jScrollPane1.setPreferredSize(new java.awt.Dimension(454, 70));
		jTable1.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

		}, new String[] {

		}));
		jScrollPane1.setViewportView(jTable1);

		jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

		splitSQL.setBottomComponent(jPanel2);

		splitTargetSource.setRightComponent(splitSQL);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(splitTargetSource, gridBagConstraints);

	}// </editor-fold>//GEN-END:initComponents

	private void fieldIDActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_fieldIDActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_fieldIDActionPerformed

	private void changeSourceQueryFocus(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_changeSourceQueryFocus
		if (evt.getKeyCode() == KeyEvent.VK_TAB) {
			if (evt.getModifiers() > 0) {
				txtSourceQuery.transferFocusBackward();
			} else {
				txtSourceQuery.transferFocus();
			}
			evt.consume();
		}
	}// GEN-LAST:event_changeSourceQueryFocus

	private void changeTargetQueryFocus(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_changeTargetQueryFocus
		if (evt.getKeyCode() == KeyEvent.VK_TAB) {
			if (evt.getModifiers() > 0) {
				txtTargetQuery.transferFocusBackward();
			} else {
				txtTargetQuery.transferFocus();
			}
			evt.consume();
		}
	}// GEN-LAST:event_changeTargetQueryFocus

	private void releaseResultset() {
		TableModel model = jTable1.getModel();
		if (model == null)
			return;
		if (!(model instanceof IncrementalResultSetTableModel))
			return;
		IncrementalResultSetTableModel imodel = (IncrementalResultSetTableModel) model;
		imodel.close();

	}

	private void cmdTestQueryActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonTestActionPerformed

		/* Cleaning the existing table and freeding resources */
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
				jTable1.setModel(model);

				// set.close();
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		// final JDialog resultquery = new JDialog();
		// resultquery.setModal(true);
		// SQLQueryPanel query_panel = new SQLQueryPanel(dataSource,
		// txtSourceQuery.getText());
		//
		// JPanel panel = new JPanel();
		// panel.setLayout(new GridBagLayout());
		// GridBagConstraints gridBagConstraints = new
		// java.awt.GridBagConstraints();
		// gridBagConstraints.gridx = 0;
		// gridBagConstraints.gridy = 0;
		// gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		// gridBagConstraints.weightx = 1.0;
		// gridBagConstraints.weighty = 1.0;
		// gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		// panel.add(query_panel, gridBagConstraints);
		//
		// resultquery.setContentPane(panel);
		// resultquery.pack();
		// resultquery.setLocationRelativeTo(null);
		// resultquery.setVisible(true);
		// resultquery.setTitle("Query Results");
	}// GEN-LAST:event_jButtonTestActionPerformed

	private class ExecuteSQLQueryAction implements OBDAProgressListener {

		CountDownLatch latch = null;
		Thread thread = null;
		ResultSet result = null;
		Statement statement = null;

		private ExecuteSQLQueryAction(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void actionCanceled() throws SQLException {
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
						TableModel oldmodel = jTable1.getModel();

						if ((oldmodel != null) && (oldmodel instanceof IncrementalResultSetTableModel)) {
							IncrementalResultSetTableModel rstm = (IncrementalResultSetTableModel) oldmodel;
							rstm.close();
						}
						JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();

						Connection c = man.getConnection(dataSource);

						Statement st = c.createStatement();
						result = st.executeQuery(txtSourceQuery.getText().trim());
						latch.countDown();
					} catch (Exception e) {
						latch.countDown();
						JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						log.error("Error while executing query.", e);
					}
				}
			};
			thread.start();
		}
	}

	private void cmdInsertMappingActionPerformed(ActionEvent e) {// GEN-FIRST:event_cmdInsertMappingActionPerformed

		releaseResultset();

		final String targetQueryString = txtTargetQuery.getText().trim();
		final String sourceQueryString = txtSourceQuery.getText().trim();

		if (fieldID.getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "ERROR: The ID cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (targetQueryString.isEmpty()) {
			JOptionPane.showMessageDialog(this, "ERROR: The target query cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (sourceQueryString.isEmpty()) {
			JOptionPane.showMessageDialog(this, "ERROR: The source query cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
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
	private javax.swing.JTextField fieldID;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTable jTable1;
	private javax.swing.JLabel labelID;
	private javax.swing.JLabel lblSourceQuery;
	private javax.swing.JLabel lblTargetQuery;
	private javax.swing.JPanel panelTrg;
	private javax.swing.JPanel pnlCommandButton;
	private javax.swing.JScrollPane scrSourceQuery;
	private javax.swing.JScrollPane scrTargetQuery;
	private javax.swing.JSplitPane splitSQL;
	private javax.swing.JSplitPane splitTargetSource;
	private javax.swing.JTextPane txtSourceQuery;
	private javax.swing.JTextPane txtTargetQuery;
	// End of variables declaration//GEN-END:variables

	private OBDAMappingAxiom mapping;

	private CQIE parse(String query) {
		TurtleSyntaxParser textParser = new TurtleSyntaxParser(controller.getPrefixManager());
		try {
			return textParser.parse(query);
		} catch (RecognitionException e) {
			// log.warn(e.getMessage());
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
		this.fieldID.setText(id);

	}

	@Override
	public void finalize() {
		releaseResultset();
	}

	/***
	 * Sets the current mapping to the input. Note, if the current mapping is
	 * set, this means that this dialog is "updating" a mapping, and not
	 * creating a new one.
	 * 
	 * @param mapping
	 */
	public void setMapping(OBDAMappingAxiom mapping) {
		cmdInsertMapping.setText("Update");
		this.mapping = mapping;
		fieldID.setText(mapping.getId());

		txtSourceQuery.setText(mapping.getSourceQuery().toString());

		TargetQueryToTurtleCodec trgcodec = new TargetQueryToTurtleCodec(this.controller);
		String trgQuery = trgcodec.encode(mapping.getTargetQuery());
		txtTargetQuery.setText(trgQuery);
	}
}
