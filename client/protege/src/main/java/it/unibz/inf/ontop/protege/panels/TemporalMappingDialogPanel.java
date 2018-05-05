package it.unibz.inf.ontop.protege.panels;

/*
 * #%L
 * ontop-protege
 * %%
 * Copyright (C) 2009 - 2018 KRDB Research Centre. Free University of Bozen Bolzano.
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
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.core.TargetQueryValidator;
import it.unibz.inf.ontop.protege.core.TemporalOBDAModel;
import it.unibz.inf.ontop.protege.gui.IconLoader;
import it.unibz.inf.ontop.protege.gui.treemodels.IncrementalResultSetTableModel;
import it.unibz.inf.ontop.protege.gui.treemodels.ResultSetTableModel;
import it.unibz.inf.ontop.protege.utils.*;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.SQLMappingFactory;
import it.unibz.inf.ontop.spec.mapping.impl.SQLMappingFactoryImpl;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.spec.mapping.parser.impl.TurtleOBDASQLParser;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.SourceQueryRenderer;
import it.unibz.inf.ontop.spec.mapping.serializer.TargetQueryRenderer;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalTriplesMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

public class TemporalMappingDialogPanel extends JPanel implements DatasourceSelectorListener {

	private static final long serialVersionUID = 4351696247473906680L;

	private TemporalOBDAModel obdaModel;
	private OBDADataSource dataSource;
	private JDialog parent;
	private static final SQLMappingFactory MAPPING_FACTORY = SQLMappingFactoryImpl.getInstance();

	private PrefixManager prefixManager;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public TemporalMappingDialogPanel(TemporalOBDAModel obdaModel, JDialog parent, OBDADataSource dataSource) {

		DialogUtils.installEscapeCloseOperation(parent);
		this.obdaModel = obdaModel;
		this.parent = parent;
		this.dataSource = dataSource;

		prefixManager = obdaModel.getMutablePrefixManager();

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

		//cmdInsertMapping.setEnabled(false);
		QueryPainter painter = new QueryPainter(obdaModel, txtTargetQuery);
		//painter.addValidatorListener(result -> cmdInsertMapping.setEnabled(result));

		cmdInsertMapping.addActionListener(this::cmdInsertMappingActionPerformed);

		txtTargetQuery.addKeyListener(new TabKeyListener());
		txtSourceQuery.addKeyListener(new TabKeyListener());
		tblQueryResult.setFocusable(false);

		txtTargetQuery.addKeyListener(new CTRLEnterKeyListener());
		txtSourceQuery.addKeyListener(new CTRLEnterKeyListener());
		txtMappingID.addKeyListener(new CTRLEnterKeyListener());

		cmdTestQuery.setFocusable(true);
		Vector<Component> order = new Vector<Component>(8);
		order.add(this.txtMappingID);
		order.add(this.txtTargetQuery);
		order.add(this.txtInterval);
		order.add(this.txtSourceQuery);
		order.add(this.cmdTestQuery);
		order.add(this.cmdInsertMapping);
		order.add(this.cmdCancel);
		this.setFocusTraversalPolicy(new CustomTraversalPolicy(order));
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
		ImmutableList<ImmutableFunctionalTerm> targetQuery = parse(target);
		if (targetQuery != null) {
			// List of invalid predicates that are found by the validator.
			List<String> invalidPredicates = TargetQueryValidator.validate(targetQuery, obdaModel.getCurrentVocabulary());
			if (invalidPredicates.isEmpty()) {
				try {
					TemporalOBDAModel mapcon = obdaModel;

					OBDASQLQuery body = MAPPING_FACTORY.getSQLQuery(source.trim());

					String newId = txtMappingID.getText().trim();
					log.info("Insert Mapping: \n"+ target + "\n" + source);

					if (mapping == null) {
						// Case when we are creating a new mapping
						OntopNativeSQLPPTriplesMap newmapping = new OntopNativeSQLPPTriplesMap(newId, body, targetQuery);
						mapcon.addTriplesMap(newmapping, false);
					} else {
						// Case when we are updating an existing mapping
						mapcon.updateMappingsSourceQuery(mapping.getId(), body);
						mapcon.updateTargetQueryMapping(mapping.getId(), targetQuery);
						mapcon.updateMapping(mapping.getId(), newId);
					}
				} catch (DuplicateMappingException e) {
					JOptionPane.showMessageDialog(this, "Error while inserting mapping: " + e.getMessage() + " is already taken");
					return;
				}
				parent.setVisible(false);
				parent.dispose();
			}
			else {
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        lblMappingID = new JLabel();
        pnlTestButton = new JPanel();
        cmdTestQuery = new JButton();
        lblTestQuery = new JLabel();
        pnlCommandButton = new JPanel();
        cmdInsertMapping = new JButton();
        cmdCancel = new JButton();
        txtMappingID = new JTextField();
        splitTargetSource = new JSplitPane();
        pnlTargetQueryEditor = new JPanel();
        lblTargetQuery = new JLabel();
        scrTargetQuery = new JScrollPane();
        txtTargetQuery = new JTextPane();
        splitSQL = new JSplitPane();
        pnlSourceQueryEditor = new JPanel();
        lblInterval = new JLabel();
		txtInterval = new JTextField();
        lblSourceQuery = new JLabel();
        scrSourceQuery = new JScrollPane();
        txtSourceQuery = new JTextPane();
        pnlQueryResult = new JPanel();
        scrQueryResult = new JScrollPane();
        tblQueryResult = new JTable();

        setFocusable(false);
        setMinimumSize(new Dimension(600, 500));
        setPreferredSize(new Dimension(600, 500));
        setLayout(new GridBagLayout());

        lblMappingID.setFont(new Font("Tahoma", 1, 11)); // NOI18N
        lblMappingID.setText("Mapping ID:");
        lblMappingID.setFocusable(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(8, 10, 8, 0);
        add(lblMappingID, gridBagConstraints);

		lblInterval.setFont(new Font("Tahoma", 1, 11)); // NOI18N
		lblInterval.setText("Interval:");
		lblInterval.setFocusable(false);
		gridBagConstraints.gridy = 1;
		add(lblInterval, gridBagConstraints);

        cmdTestQuery.setIcon(IconLoader.getImageIcon("images/execute.png"));
        cmdTestQuery.setMnemonic('t');
        cmdTestQuery.setText("Test SQL Query");
        cmdTestQuery.setToolTipText("Execute the SQL query in the SQL query text pane\nand display the first 100 results in the table.");
        cmdTestQuery.setActionCommand("Test SQL query");
        cmdTestQuery.setBorder(BorderFactory.createEtchedBorder());
        cmdTestQuery.setContentAreaFilled(false);
        cmdTestQuery.setIconTextGap(5);
        cmdTestQuery.setMaximumSize(new Dimension(115, 25));
        cmdTestQuery.setMinimumSize(new Dimension(115, 25));
        cmdTestQuery.setPreferredSize(new Dimension(115, 25));
        cmdTestQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cmdTestQueryActionPerformed(evt);
            }
        });
        pnlTestButton.add(cmdTestQuery);

        lblTestQuery.setText("(100 rows)");
        pnlTestButton.add(lblTestQuery);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(4, 10, 0, 0);
        add(pnlTestButton, gridBagConstraints);

        pnlCommandButton.setFocusable(false);
        pnlCommandButton.setLayout(new FlowLayout(FlowLayout.RIGHT));

        cmdInsertMapping.setIcon(IconLoader.getImageIcon("images/accept.png"));
        cmdInsertMapping.setText("Accept");
        cmdInsertMapping.setToolTipText("This will add/edit the current mapping into the OBDA model");
        cmdInsertMapping.setActionCommand("OK");
        cmdInsertMapping.setBorder(BorderFactory.createEtchedBorder());
        cmdInsertMapping.setContentAreaFilled(false);
        cmdInsertMapping.setIconTextGap(5);
        cmdInsertMapping.setPreferredSize(new Dimension(90, 25));
        pnlCommandButton.add(cmdInsertMapping);

        cmdCancel.setIcon(IconLoader.getImageIcon("images/cancel.png"));
        cmdCancel.setText("Cancel");
        cmdCancel.setBorder(BorderFactory.createEtchedBorder());
        cmdCancel.setContentAreaFilled(false);
        cmdCancel.setIconTextGap(5);
        cmdCancel.setPreferredSize(new Dimension(90, 25));
        cmdCancel.addActionListener(this::cmdCancelActionPerformed);
        pnlCommandButton.add(cmdCancel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(0, 0, 10, 4);
        add(pnlCommandButton, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(8, 0, 8, 10);
        add(txtMappingID, gridBagConstraints);

		gridBagConstraints.gridy = 1;
        add(txtInterval, gridBagConstraints);

        splitTargetSource.setBorder(null);
        splitTargetSource.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitTargetSource.setResizeWeight(0.5);
        splitTargetSource.setDoubleBuffered(true);
        splitTargetSource.setFocusable(false);
        splitTargetSource.setMinimumSize(new Dimension(600, 430));
        splitTargetSource.setOneTouchExpandable(true);
        splitTargetSource.setPreferredSize(new Dimension(600, 430));

        pnlTargetQueryEditor.setFocusable(false);
        pnlTargetQueryEditor.setMinimumSize(new Dimension(600, 180));
        pnlTargetQueryEditor.setPreferredSize(new Dimension(600, 180));
        pnlTargetQueryEditor.setLayout(new BorderLayout());

        lblTargetQuery.setFont(new Font("Tahoma", 1, 11)); // NOI18N
        lblTargetQuery.setText("Target (Triples Template):");
        lblTargetQuery.setFocusable(false);
        pnlTargetQueryEditor.add(lblTargetQuery, BorderLayout.NORTH);

        scrTargetQuery.setFocusable(false);
        scrTargetQuery.setMinimumSize(new Dimension(600, 170));
        scrTargetQuery.setPreferredSize(new Dimension(600, 170));

        txtTargetQuery.setFont(new Font("Lucida Sans Typewriter", 0, 13)); // NOI18N
        txtTargetQuery.setFocusCycleRoot(false);
        txtTargetQuery.setMinimumSize(new Dimension(600, 170));
        txtTargetQuery.setPreferredSize(new Dimension(600, 170));
        scrTargetQuery.setViewportView(txtTargetQuery);

        pnlTargetQueryEditor.add(scrTargetQuery, BorderLayout.CENTER);

        splitTargetSource.setLeftComponent(pnlTargetQueryEditor);

        splitSQL.setBorder(null);
        splitSQL.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitSQL.setResizeWeight(0.8);
        splitSQL.setFocusable(false);
        splitSQL.setMinimumSize(new Dimension(600, 280));
        splitSQL.setOneTouchExpandable(true);
        splitSQL.setPreferredSize(new Dimension(600, 280));

        pnlSourceQueryEditor.setFocusable(false);
        pnlSourceQueryEditor.setMinimumSize(new Dimension(600, 150));
        pnlSourceQueryEditor.setPreferredSize(new Dimension(600, 150));
        pnlSourceQueryEditor.setLayout(new BorderLayout());

        lblSourceQuery.setFont(new Font("Tahoma", 1, 11)); // NOI18N
        lblSourceQuery.setText("Source (SQL Query):");
        lblSourceQuery.setFocusable(false);
        pnlSourceQueryEditor.add(lblSourceQuery, BorderLayout.NORTH);

        scrSourceQuery.setFocusable(false);

        txtSourceQuery.setFont(new Font("Lucida Sans Typewriter", 0, 13)); // NOI18N
        txtSourceQuery.setFocusCycleRoot(false);
        scrSourceQuery.setViewportView(txtSourceQuery);

        pnlSourceQueryEditor.add(scrSourceQuery, BorderLayout.CENTER);

        splitSQL.setTopComponent(pnlSourceQueryEditor);

        pnlQueryResult.setFocusable(false);
        pnlQueryResult.setMinimumSize(new Dimension(600, 120));
        pnlQueryResult.setPreferredSize(new Dimension(600, 120));
        pnlQueryResult.setLayout(new BorderLayout());

        scrQueryResult.setFocusable(false);
        scrQueryResult.setPreferredSize(new Dimension(454, 70));

        tblQueryResult.setMinimumSize(new Dimension(600, 180));
        scrQueryResult.setViewportView(tblQueryResult);

        pnlQueryResult.add(scrQueryResult, BorderLayout.CENTER);

        splitSQL.setBottomComponent(pnlQueryResult);

        splitTargetSource.setRightComponent(splitSQL);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 10, 0, 10);
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

	private void cmdTestQueryActionPerformed(ActionEvent evt) {// GEN-FIRST:event_jButtonTestActionPerformed
		// Cleaning the existing table and releasing resources
		releaseResultset();

		OBDAProgressMonitor progMonitor = new OBDAProgressMonitor("Executing query...", this);
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
				ResultSetTableModel model = new ResultSetTableModel(set);
				tblQueryResult.setModel(model);
				scrQueryResult.getParent().revalidate();

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

						if ((oldmodel != null) && (oldmodel instanceof ResultSetTableModel)) {
							ResultSetTableModel rstm = (ResultSetTableModel) oldmodel;
							rstm.close();
						}
//						JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();
						Connection c = ConnectionTools.getConnection(dataSource);

//						String driver = dataSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

						Statement st = c.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
						st.setMaxRows(100);
						result = st.executeQuery(txtSourceQuery.getText().trim());
						latch.countDown();
					} catch (Exception e) {
						latch.countDown();
						DialogUtils.showQuickErrorDialog(getRootPane(), e);
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

	private void cmdCancelActionPerformed(ActionEvent evt) {// GEN-FIRST:event_cmdCancelActionPerformed
		parent.setVisible(false);
		parent.dispose();
		releaseResultset();
	}// GEN-LAST:event_cmdCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton cmdCancel;
    private JButton cmdInsertMapping;
    private JButton cmdTestQuery;
    private JLabel lblMappingID;
    private JLabel lblSourceQuery;
    private JLabel lblInterval;
    private JLabel lblTargetQuery;
    private JLabel lblTestQuery;
    private JPanel pnlCommandButton;
    private JPanel pnlQueryResult;
    private JPanel pnlSourceQueryEditor;
    private JPanel pnlTargetQueryEditor;
    private JPanel pnlTestButton;
    private JScrollPane scrQueryResult;
    private JScrollPane scrSourceQuery;
    private JScrollPane scrTargetQuery;
    private JSplitPane splitSQL;
    private JSplitPane splitTargetSource;
    private JTable tblQueryResult;
    private JTextField txtMappingID;
    private JTextPane txtSourceQuery;
    private JTextPane txtTargetQuery;
    private JTextField txtInterval;
    // End of variables declaration//GEN-END:variables

	private SQLPPTemporalTriplesMap mapping;

	private ImmutableList<ImmutableFunctionalTerm> parse(String query) {
        TargetQueryParser textParser = new TurtleOBDASQLParser(obdaModel.getMutablePrefixManager().getPrefixMap(),
				obdaModel.getAtomFactory(), obdaModel.getTermFactory());
		try {
			return textParser.parse(query);
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
	public void setMapping(SQLPPTemporalTriplesMap mapping) {
		this.mapping = mapping;

		cmdInsertMapping.setText("Update");
		txtMappingID.setText(mapping.getId());

		OBDASQLQuery sourceQuery = mapping.getSourceQuery();
		String srcQuery = SourceQueryRenderer.encode(sourceQuery);
		txtSourceQuery.setText(srcQuery);

		ImmutableList<ImmutableFunctionalTerm> targetQuery = mapping.getTargetAtoms();
		String trgQuery = TargetQueryRenderer.encode(targetQuery, prefixManager);
		txtTargetQuery.setText(trgQuery);
	}
}
