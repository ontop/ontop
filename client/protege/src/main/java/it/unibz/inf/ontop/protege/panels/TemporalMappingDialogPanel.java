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
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.protege.core.TemporalOBDAModel;
import it.unibz.inf.ontop.protege.gui.IconLoader;
import it.unibz.inf.ontop.protege.gui.treemodels.IncrementalResultSetTableModel;
import it.unibz.inf.ontop.protege.gui.treemodels.ResultSetTableModel;
import it.unibz.inf.ontop.protege.utils.*;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.serializer.SourceQueryRenderer;
import it.unibz.inf.ontop.spec.mapping.serializer.TargetQueryRenderer;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalTriplesMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

public class TemporalMappingDialogPanel extends JPanel {
    private final TemporalOBDAModel obdaModel;
    private final JDialog parent;

	private final Logger log = LoggerFactory.getLogger(this.getClass());
    private JButton btnTestQuery;

    TemporalMappingDialogPanel(TemporalOBDAModel obdaModel, JDialog parent) {

		DialogUtils.installEscapeCloseOperation(parent);
		this.obdaModel = obdaModel;
		this.parent = parent;

		initComponents();

		cmdInsertMapping.addActionListener(e -> insertMapping());

		txtTargetQuery.addKeyListener(new TabKeyListener());
		txtSourceQuery.addKeyListener(new TabKeyListener());
		tblQueryResult.setFocusable(false);

		txtTargetQuery.addKeyListener(new CTRLEnterKeyListener());
		txtSourceQuery.addKeyListener(new CTRLEnterKeyListener());
		txtMappingID.addKeyListener(new CTRLEnterKeyListener());

        btnTestQuery.setFocusable(true);
        Vector<Component> order = new Vector<>(8);
		order.add(this.txtMappingID);
		order.add(this.txtTargetQuery);
		order.add(this.txtInterval);
		order.add(this.txtSourceQuery);
        order.add(this.btnTestQuery);
		order.add(this.cmdInsertMapping);
		order.add(this.cmdCancel);
		this.setFocusTraversalPolicy(new CustomTraversalPolicy(order));
	}

	private void insertMapping() {
		releaseResultset();

		final String targetQueryString = txtTargetQuery.getText();
		final String sourceQueryString = txtSourceQuery.getText();
		final String mappingId = txtMappingID.getText().trim();
		final String interval = txtInterval.getText().trim();

		if (mappingId.isEmpty()) {
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
		if (interval.isEmpty()) {
			JOptionPane.showMessageDialog(this, "ERROR: The interval cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
        try {
            if (obdaModel.insertMapping(mapping, mappingId, targetQueryString, sourceQueryString, interval)) {
                parent.setVisible(false);
                parent.dispose();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "ERROR: An error occured when saving the mapping:" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void testQueryActionPerformed() {
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
    }

    private void releaseResultset() {
        TableModel model = tblQueryResult.getModel();
        if (model == null)
            return;
        if (!(model instanceof IncrementalResultSetTableModel))
            return;
        IncrementalResultSetTableModel imodel = (IncrementalResultSetTableModel) model;
        imodel.close();
    }

    private void cmdCancelActionPerformed() {
        parent.setVisible(false);
        parent.dispose();
        releaseResultset();
    }

    private class CTRLEnterKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
            // NO-OP
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (cmdInsertMapping.isEnabled() && (e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() == KeyEvent.VK_ENTER)) {
                insertMapping();
            } else if ((e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyCode() == KeyEvent.VK_T)) {
                testQueryActionPerformed();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        JLabel lblMappingID = new JLabel();
        JPanel pnlTestButton = new JPanel();
        btnTestQuery = new JButton();
        JLabel lblTestQuery = new JLabel();
        JPanel pnlCommandButton = new JPanel();
        cmdInsertMapping = new JButton();
        cmdCancel = new JButton();
        txtMappingID = new JTextField();
        JSplitPane splitTargetSource = new JSplitPane();
        JPanel pnlTargetQueryEditor = new JPanel();
        JLabel lblTargetQuery = new JLabel();
        JScrollPane scrTargetQuery = new JScrollPane();
        txtTargetQuery = new JTextPane();
        JSplitPane splitSQL = new JSplitPane();
        JPanel pnlSourceQueryEditor = new JPanel();
        JLabel lblInterval = new JLabel();
		txtInterval = new JTextField();
        JLabel lblSourceQuery = new JLabel();
        JScrollPane scrSourceQuery = new JScrollPane();
        txtSourceQuery = new JTextPane();
        JPanel pnlQueryResult = new JPanel();
        scrQueryResult = new JScrollPane();
        tblQueryResult = new JTable();

        setFocusable(false);
        setPreferredSize(new Dimension(600, 500));
        setLayout(new GridBagLayout());

        lblMappingID.setText("Mapping ID:");
        lblMappingID.setFocusable(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(8, 10, 8, 0);
        add(lblMappingID, gridBagConstraints);

		lblInterval.setText("Interval:");
		lblInterval.setFocusable(false);
		gridBagConstraints.gridy = 1;
		add(lblInterval, gridBagConstraints);

        btnTestQuery.setIcon(IconLoader.getImageIcon("images/execute.png"));
        btnTestQuery.setMnemonic('t');
        btnTestQuery.setText("Test SQL Query");
        btnTestQuery.setToolTipText("Execute the SQL query in the SQL query text pane\nand display the first 100 results in the table.");
        btnTestQuery.setActionCommand("Test SQL query");
        btnTestQuery.setBorder(BorderFactory.createEtchedBorder());
        btnTestQuery.setContentAreaFilled(false);
        btnTestQuery.setIconTextGap(5);
        btnTestQuery.setPreferredSize(new Dimension(115, 25));
        btnTestQuery.addActionListener(e -> testQueryActionPerformed());
        pnlTestButton.add(btnTestQuery);

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
        cmdCancel.addActionListener(e -> cmdCancelActionPerformed());
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
        splitTargetSource.setOneTouchExpandable(true);
        splitTargetSource.setPreferredSize(new Dimension(600, 430));

        pnlTargetQueryEditor.setFocusable(false);
        pnlTargetQueryEditor.setPreferredSize(new Dimension(600, 180));
        pnlTargetQueryEditor.setLayout(new BorderLayout());

        lblTargetQuery.setText("Target (Triples Template):");
        lblTargetQuery.setFocusable(false);
        pnlTargetQueryEditor.add(lblTargetQuery, BorderLayout.NORTH);

        scrTargetQuery.setFocusable(false);
        scrTargetQuery.setPreferredSize(new Dimension(600, 170));

        txtTargetQuery.setFocusCycleRoot(false);
        txtTargetQuery.setPreferredSize(new Dimension(600, 170));
        scrTargetQuery.setViewportView(txtTargetQuery);

        pnlTargetQueryEditor.add(scrTargetQuery, BorderLayout.CENTER);

        splitTargetSource.setLeftComponent(pnlTargetQueryEditor);

        splitSQL.setBorder(null);
        splitSQL.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitSQL.setResizeWeight(0.8);
        splitSQL.setFocusable(false);
        splitSQL.setOneTouchExpandable(true);
        splitSQL.setPreferredSize(new Dimension(600, 280));

        pnlSourceQueryEditor.setFocusable(false);
        pnlSourceQueryEditor.setPreferredSize(new Dimension(600, 150));
        pnlSourceQueryEditor.setLayout(new BorderLayout());

        lblSourceQuery.setText("Source (SQL Query):");
        lblSourceQuery.setFocusable(false);
        pnlSourceQueryEditor.add(lblSourceQuery, BorderLayout.NORTH);

        scrSourceQuery.setFocusable(false);

        txtSourceQuery.setFocusCycleRoot(false);
        scrSourceQuery.setViewportView(txtSourceQuery);

        pnlSourceQueryEditor.add(scrSourceQuery, BorderLayout.CENTER);

        splitSQL.setTopComponent(pnlSourceQueryEditor);

        pnlQueryResult.setFocusable(false);
        pnlQueryResult.setPreferredSize(new Dimension(600, 120));
        pnlQueryResult.setLayout(new BorderLayout());

        scrQueryResult.setFocusable(false);
        scrQueryResult.setPreferredSize(new Dimension(454, 70));

        tblQueryResult.setPreferredSize(new Dimension(600, 180));
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
    }

    private JButton cmdCancel;
    private JButton cmdInsertMapping;

	private class ExecuteSQLQueryAction implements OBDAProgressListener {

        final CountDownLatch latch;
		Thread thread = null;
		ResultSet result = null;
        final Statement statement = null;
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

        ResultSet getResult() {
			return result;
		}

        void run() {
            thread = new Thread(() -> {
                try {
                    TableModel oldmodel = tblQueryResult.getModel();

                    if ((oldmodel instanceof ResultSetTableModel)) {
                        ResultSetTableModel rstm = (ResultSetTableModel) oldmodel;
                        rstm.close();
                    }
                    Connection c = ConnectionTools.getConnection(obdaModel.getDatasource());
                    Statement st = c.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    st.setMaxRows(100);
                    result = st.executeQuery(txtSourceQuery.getText().trim());
                    latch.countDown();
                } catch (Exception e) {
                    latch.countDown();
                    DialogUtils.showQuickErrorDialog(getRootPane(), e);
                    errorShown = true;
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
    private JScrollPane scrQueryResult;
    private JTable tblQueryResult;
    private JTextField txtMappingID;
    private JTextPane txtSourceQuery;
    private JTextPane txtTargetQuery;
    private JTextField txtInterval;

	private SQLPPTemporalTriplesMap mapping;

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
        String trgQuery = TargetQueryRenderer.encode(targetQuery, obdaModel.getMutablePrefixManager());
		txtTargetQuery.setText(trgQuery);

		txtInterval.setText(mapping.getTemporalMappingInterval().toString());
	}
}
