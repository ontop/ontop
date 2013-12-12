package it.unibz.krdb.obda.protege4.panels;

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

import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAModelListener;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.protege4.gui.IconLoader;
import it.unibz.krdb.obda.protege4.utils.CustomTraversalPolicy;
import it.unibz.krdb.obda.protege4.utils.DatasourceSelectorListener;
import it.unibz.krdb.obda.protege4.utils.DialogUtils;
import it.unibz.krdb.sql.JDBCConnectionManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class DatasourceParameterEditorPanel extends javax.swing.JPanel implements DatasourceSelectorListener {

	private static final long serialVersionUID = 3506358479342412849L;

	private OBDADataSource selectedDataSource;

	private OBDAModel obdaModel;

	private DatasourceSelector selector;

	private ComboBoxItemListener comboListener;

	private Timer timer = null;

	/**
	 * Creates new form DatasourceParameterEditorPanel
	 */
	public DatasourceParameterEditorPanel(OBDAModel model) {

		timer = new Timer(200, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleTimer();
			}
		});
		
		initComponents();

		this.comboListener = new ComboBoxItemListener();
		txtJdbcDriver.addItemListener(comboListener);

		setDatasourcesController(model);
		enableFields(false);

		Vector<Component> order = new Vector<Component>(7);
		order.add(pnlDataSourceParameters);
		order.add(txtJdbcUrl);
		order.add(txtDatabaseUsername);
		order.add(txtDatabasePassword);
		order.add(txtJdbcDriver);
		order.add(cmdTestConnection);
		this.setFocusTraversalPolicy(new CustomTraversalPolicy(order));

		lblSourcesNumber.setText(Integer.toString(obdaModel.getSources().size()));
		model.addSourcesListener(new OBDAModelListener() {
			private static final long serialVersionUID = -415753131971100104L;

			@Override
			public void datasourceUpdated(String oldname, OBDADataSource currendata) {
				// NO OP
			}

			@Override
			public void datasourceDeleted(OBDADataSource source) {
				lblSourcesNumber.setText(Integer.toString(obdaModel.getSources().size()));
			}

			@Override
			public void datasourceAdded(OBDADataSource source) {
				lblSourcesNumber.setText(Integer.toString(obdaModel.getSources().size()));
			}

			@Override
			public void datasourcParametersUpdated() {
				// NO OP
			}

			@Override
			public void alldatasourcesDeleted() {
				lblSourcesNumber.setText(Integer.toString(obdaModel.getSources().size()));
			}
		});

		
	}

	private void handleTimer() {
		timer.stop();
		updateSourceValues();
	}

	private class ComboBoxItemListener implements ItemListener {

		private boolean notify = false;

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (notify) {
				fieldChangeHandler(null);
			}
		}

		public void setNotify(boolean notify) {
			this.notify = notify;
		}
	}

	public void setDatasourcesController(OBDAModel model) {
		obdaModel = model;
		addDataSourceSelector();
		resetTextFields();
	}

	private void addDataSourceSelector() {
		selector = new DatasourceSelector(obdaModel);
		selector.addDatasourceListListener(this);
		pnlDataSourceSelector.add(selector, BorderLayout.CENTER);
	}

	private void resetTextFields() {
		txtJdbcUrl.setText("");
		txtDatabasePassword.setText("");
		txtDatabaseUsername.setText("");
		comboListener.setNotify(false);
		txtJdbcDriver.setSelectedIndex(0);
		comboListener.setNotify(true);
	}

	private void enableFields(boolean value) {
		txtJdbcUrl.setEnabled(value);
		txtDatabasePassword.setEnabled(value);
		txtDatabaseUsername.setEnabled(value);
		txtJdbcDriver.setEnabled(value);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		pnlOBDAModelData = new javax.swing.JPanel();
		lblSources = new javax.swing.JLabel();
		lblSourcesNumber = new javax.swing.JLabel();
		jPanel1 = new javax.swing.JPanel();
		pnlDataSourceParameters = new javax.swing.JPanel();
		txtJdbcUrl = new javax.swing.JTextField();
		txtDatabaseUsername = new javax.swing.JTextField();
		txtDatabasePassword = new javax.swing.JPasswordField();
		txtJdbcDriver = new javax.swing.JComboBox();
		cmdTestConnection = new javax.swing.JButton();
		lblDataSourceName = new javax.swing.JLabel();
		lblJdbcUrl = new javax.swing.JLabel();
		lblDatabaseUsername = new javax.swing.JLabel();
		lblDatabasePassword = new javax.swing.JLabel();
		lblJdbcDriver = new javax.swing.JLabel();
		lblConnectionStatus = new javax.swing.JLabel();
		pnlDataSourceSelector = new javax.swing.JPanel();
		pnlCommandButton = new javax.swing.JPanel();
		cmdNew = new javax.swing.JButton();
		cmdRemove = new javax.swing.JButton();
		cmdHelp = new javax.swing.JButton();
		pnlInformation = new javax.swing.JPanel();

		setFocusable(false);
		setMinimumSize(new java.awt.Dimension(640, 480));
		setPreferredSize(new java.awt.Dimension(640, 480));
		setLayout(new java.awt.GridBagLayout());

		pnlOBDAModelData.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "OBDA Model information",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font(
						"Lucida Grande", 0, 13), new java.awt.Color(53, 113, 163))); // NOI18N
		pnlOBDAModelData.setForeground(new java.awt.Color(53, 113, 163));
		pnlOBDAModelData.setLayout(new java.awt.GridBagLayout());

		lblSources.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
		lblSources.setForeground(new java.awt.Color(53, 113, 163));
		lblSources.setText("Number of sources:");
		lblSources.setFocusTraversalKeysEnabled(false);
		lblSources.setFocusable(false);
		lblSources.setMaximumSize(new java.awt.Dimension(120, 24));
		lblSources.setMinimumSize(new java.awt.Dimension(120, 24));
		lblSources.setPreferredSize(new java.awt.Dimension(130, 24));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
		gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 20);
		pnlOBDAModelData.add(lblSources, gridBagConstraints);

		lblSourcesNumber.setFont(new java.awt.Font("Courier New", 1, 13)); // NOI18N
		lblSourcesNumber.setText("0");
		lblSourcesNumber.setFocusable(false);
		lblSourcesNumber.setPreferredSize(new java.awt.Dimension(150, 24));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
		gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 20);
		pnlOBDAModelData.add(lblSourcesNumber, gridBagConstraints);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		pnlOBDAModelData.add(jPanel1, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		add(pnlOBDAModelData, gridBagConstraints);

		pnlDataSourceParameters.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Connection parameters",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font(
						"Lucida Grande", 0, 13), new java.awt.Color(53, 113, 163))); // NOI18N
		pnlDataSourceParameters.setForeground(new java.awt.Color(53, 113, 163));
		pnlDataSourceParameters.setAlignmentX(5.0F);
		pnlDataSourceParameters.setAlignmentY(5.0F);
		pnlDataSourceParameters.setAutoscrolls(true);
		pnlDataSourceParameters.setFocusable(false);
		pnlDataSourceParameters.setMaximumSize(new java.awt.Dimension(32767, 23));
		pnlDataSourceParameters.setMinimumSize(new java.awt.Dimension(0, 0));
		pnlDataSourceParameters.setPreferredSize(new java.awt.Dimension(1, 230));
		pnlDataSourceParameters.setLayout(new java.awt.GridBagLayout());

		txtJdbcUrl.setFont(new java.awt.Font("Courier New", 1, 13)); // NOI18N
		txtJdbcUrl.setMaximumSize(new java.awt.Dimension(25, 2147483647));
		txtJdbcUrl.setMinimumSize(new java.awt.Dimension(180, 24));
		txtJdbcUrl.setPreferredSize(new java.awt.Dimension(180, 24));
		txtJdbcUrl.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(java.awt.event.KeyEvent evt) {
				fieldChangeHandler(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(10, 0, 2, 10);
		pnlDataSourceParameters.add(txtJdbcUrl, gridBagConstraints);

		txtDatabaseUsername.setFont(new java.awt.Font("Courier New", 1, 13)); // NOI18N
		txtDatabaseUsername.setMaximumSize(new java.awt.Dimension(25, 2147483647));
		txtDatabaseUsername.setMinimumSize(new java.awt.Dimension(180, 24));
		txtDatabaseUsername.setPreferredSize(new java.awt.Dimension(180, 24));
		txtDatabaseUsername.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(java.awt.event.KeyEvent evt) {
				fieldChangeHandler(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(3, 0, 2, 10);
		pnlDataSourceParameters.add(txtDatabaseUsername, gridBagConstraints);

		txtDatabasePassword.setFont(new java.awt.Font("Courier New", 1, 13)); // NOI18N
		txtDatabasePassword.setMinimumSize(new java.awt.Dimension(180, 24));
		txtDatabasePassword.setPreferredSize(new java.awt.Dimension(180, 24));
		txtDatabasePassword.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(java.awt.event.KeyEvent evt) {
				fieldChangeHandler(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.insets = new java.awt.Insets(3, 0, 2, 10);
		pnlDataSourceParameters.add(txtDatabasePassword, gridBagConstraints);

		txtJdbcDriver.setEditable(true);
		txtJdbcDriver.setFont(new java.awt.Font("Courier New", 1, 13)); // NOI18N
		txtJdbcDriver.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "select or type the JDBC Driver's class...",
				"org.postgresql.Driver", "com.mysql.jdbc.Driver", "org.h2.Driver", "com.ibm.db2.jcc.DB2Driver",
				"oracle.jdbc.OracleDriver", "com.microsoft.sqlserver.jdbc.SQLServerDriver" }));
		txtJdbcDriver.setMinimumSize(new java.awt.Dimension(180, 24));
		txtJdbcDriver.setPreferredSize(new java.awt.Dimension(180, 24));
		txtJdbcDriver.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				txtJdbcDriverActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.insets = new java.awt.Insets(3, 0, 2, 10);
		pnlDataSourceParameters.add(txtJdbcDriver, gridBagConstraints);

		cmdTestConnection.setIcon(IconLoader.getImageIcon("images/execute.png"));
		cmdTestConnection.setText("Test Connection");
		cmdTestConnection.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdTestConnection.setContentAreaFilled(false);
		cmdTestConnection.setIconTextGap(5);
		cmdTestConnection.setMaximumSize(new java.awt.Dimension(110, 25));
		cmdTestConnection.setMinimumSize(new java.awt.Dimension(110, 25));
		cmdTestConnection.setPreferredSize(new java.awt.Dimension(110, 25));
		cmdTestConnection.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cmdTestConnectionActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.insets = new java.awt.Insets(8, 10, 10, 20);
		pnlDataSourceParameters.add(cmdTestConnection, gridBagConstraints);

		lblDataSourceName.setBackground(new java.awt.Color(153, 153, 153));
		lblDataSourceName.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
		lblDataSourceName.setForeground(new java.awt.Color(53, 113, 163));
		lblDataSourceName.setText("Datasource Name:   ");
		lblDataSourceName.setFocusTraversalKeysEnabled(false);
		lblDataSourceName.setFocusable(false);
		lblDataSourceName.setMaximumSize(new java.awt.Dimension(120, 27));
		lblDataSourceName.setMinimumSize(new java.awt.Dimension(120, 27));
		lblDataSourceName.setPreferredSize(new java.awt.Dimension(120, 27));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
		gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
		pnlDataSourceParameters.add(lblDataSourceName, gridBagConstraints);

		lblJdbcUrl.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
		lblJdbcUrl.setForeground(new java.awt.Color(53, 113, 163));
		lblJdbcUrl.setText("Connection URL:");
		lblJdbcUrl.setFocusTraversalKeysEnabled(false);
		lblJdbcUrl.setFocusable(false);
		lblJdbcUrl.setMaximumSize(new java.awt.Dimension(130, 24));
		lblJdbcUrl.setMinimumSize(new java.awt.Dimension(130, 24));
		lblJdbcUrl.setPreferredSize(new java.awt.Dimension(130, 24));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.insets = new java.awt.Insets(10, 10, 2, 20);
		pnlDataSourceParameters.add(lblJdbcUrl, gridBagConstraints);

		lblDatabaseUsername.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
		lblDatabaseUsername.setForeground(new java.awt.Color(53, 113, 163));
		lblDatabaseUsername.setText("Database Username:");
		lblDatabaseUsername.setFocusTraversalKeysEnabled(false);
		lblDatabaseUsername.setFocusable(false);
		lblDatabaseUsername.setMaximumSize(new java.awt.Dimension(130, 24));
		lblDatabaseUsername.setMinimumSize(new java.awt.Dimension(130, 24));
		lblDatabaseUsername.setPreferredSize(new java.awt.Dimension(130, 24));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.insets = new java.awt.Insets(3, 10, 2, 20);
		pnlDataSourceParameters.add(lblDatabaseUsername, gridBagConstraints);

		lblDatabasePassword.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
		lblDatabasePassword.setForeground(new java.awt.Color(53, 113, 163));
		lblDatabasePassword.setText("Database Password:");
		lblDatabasePassword.setFocusTraversalKeysEnabled(false);
		lblDatabasePassword.setFocusable(false);
		lblDatabasePassword.setMaximumSize(new java.awt.Dimension(130, 24));
		lblDatabasePassword.setMinimumSize(new java.awt.Dimension(130, 24));
		lblDatabasePassword.setPreferredSize(new java.awt.Dimension(130, 24));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.insets = new java.awt.Insets(3, 10, 2, 20);
		pnlDataSourceParameters.add(lblDatabasePassword, gridBagConstraints);

		lblJdbcDriver.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
		lblJdbcDriver.setForeground(new java.awt.Color(53, 113, 163));
		lblJdbcDriver.setText("Driver class:");
		lblJdbcDriver.setFocusTraversalKeysEnabled(false);
		lblJdbcDriver.setFocusable(false);
		lblJdbcDriver.setMaximumSize(new java.awt.Dimension(130, 24));
		lblJdbcDriver.setMinimumSize(new java.awt.Dimension(130, 24));
		lblJdbcDriver.setPreferredSize(new java.awt.Dimension(130, 24));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.insets = new java.awt.Insets(3, 10, 2, 20);
		pnlDataSourceParameters.add(lblJdbcDriver, gridBagConstraints);

		lblConnectionStatus.setFont(new java.awt.Font("Courier New", 1, 13)); // NOI18N
		lblConnectionStatus.setFocusTraversalKeysEnabled(false);
		lblConnectionStatus.setFocusable(false);
		lblConnectionStatus.setMaximumSize(new java.awt.Dimension(180, 24));
		lblConnectionStatus.setMinimumSize(new java.awt.Dimension(180, 24));
		lblConnectionStatus.setPreferredSize(new java.awt.Dimension(180, 24));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.gridheight = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.insets = new java.awt.Insets(8, 0, 10, 10);
		pnlDataSourceParameters.add(lblConnectionStatus, gridBagConstraints);

		pnlDataSourceSelector.setFocusable(false);
		pnlDataSourceSelector.setMinimumSize(new java.awt.Dimension(300, 27));
		pnlDataSourceSelector.setPreferredSize(new java.awt.Dimension(300, 27));
		pnlDataSourceSelector.setLayout(new java.awt.BorderLayout());
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		pnlDataSourceParameters.add(pnlDataSourceSelector, gridBagConstraints);

		pnlCommandButton.setFocusable(false);
		pnlCommandButton.setMinimumSize(new java.awt.Dimension(210, 27));
		pnlCommandButton.setPreferredSize(new java.awt.Dimension(210, 27));
		pnlCommandButton.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));

		cmdNew.setIcon(IconLoader.getImageIcon("images/plus.png"));
		cmdNew.setText("Create New...");
		cmdNew.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdNew.setContentAreaFilled(false);
		cmdNew.setIconTextGap(5);
		cmdNew.setMargin(new java.awt.Insets(0, 0, 0, 0));
		cmdNew.setMaximumSize(new java.awt.Dimension(105, 25));
		cmdNew.setMinimumSize(new java.awt.Dimension(105, 25));
		cmdNew.setPreferredSize(new java.awt.Dimension(105, 25));
		cmdNew.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cmdNewActionPerformed(evt);
			}
		});
		pnlCommandButton.add(cmdNew);

		cmdRemove.setIcon(IconLoader.getImageIcon("images/minus.png"));
		cmdRemove.setText("Remove");
		cmdRemove.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		cmdRemove.setContentAreaFilled(false);
		cmdRemove.setIconTextGap(5);
		cmdRemove.setMaximumSize(new java.awt.Dimension(85, 25));
		cmdRemove.setMinimumSize(new java.awt.Dimension(85, 25));
		cmdRemove.setPreferredSize(new java.awt.Dimension(85, 25));
		cmdRemove.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cmdRemoveActionPerformed(evt);
			}
		});
		pnlCommandButton.add(cmdRemove);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
		pnlDataSourceParameters.add(pnlCommandButton, gridBagConstraints);

		cmdHelp.setFont(new java.awt.Font("Dialog", 1, 13)); // NOI18N
		cmdHelp.setForeground(new java.awt.Color(53, 113, 163));
		cmdHelp.setIcon(IconLoader.getImageIcon("images/gtk-help.png"));
		cmdHelp.setText("<HTML><U>Help</U></HTML>");
		cmdHelp.setToolTipText("For information on JDBC connections go to: https://babbage.inf.unibz.it/trac/obdapublic/wiki/ObdalibPluginJDBC");
		cmdHelp.setBorderPainted(false);
		cmdHelp.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cmdHelpActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		pnlDataSourceParameters.add(cmdHelp, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;
		add(pnlDataSourceParameters, gridBagConstraints);

		pnlInformation.setFocusable(false);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		add(pnlInformation, gridBagConstraints);
	}// </editor-fold>//GEN-END:initComponents

	private void cmdHelpActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdHelpActionPerformed
		DialogUtils.open(URI.create("https://babbage.inf.unibz.it/trac/obdapublic/wiki/ObdalibPluginJDBC"));
	}// GEN-LAST:event_cmdHelpActionPerformed

	private void txtJdbcDriverActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txtJdbcDriverActionPerformed
		fieldChangeHandler(null);
	}// GEN-LAST:event_txtJdbcDriverActionPerformed

	private void cmdNewActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdNewActionPerformed
		while (true) {
			String name = JOptionPane.showInputDialog(this, "Insert an identifier for the new data source:", null);
			if (name == null) {
				return;
			}

			if (!name.isEmpty()) {
				URI uri = createUri(name);
				if (uri != null) {
					if (!obdaModel.containsSource(uri)) {
						OBDADataSource source = OBDADataFactoryImpl.getInstance().getDataSource(uri);
						obdaModel.addSource(source);
						selector.set(source);
						return;
					} else {
						JOptionPane.showMessageDialog(this, "The specified data source ID already exists. \nPlease provide a different one.", "Warning", JOptionPane.WARNING_MESSAGE);
					}
				}
			} else {
				JOptionPane.showMessageDialog(this, "The data source ID cannot be blank", "Warning", JOptionPane.WARNING_MESSAGE);
			}
		}
	}// GEN-LAST:event_cmdNewActionPerformed

	private void cmdRemoveActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdRemoveActionPerformed
		OBDADataSource ds = selector.getSelectedDataSource();
		if (ds == null) {
			JOptionPane.showMessageDialog(this, "Select a data source to proceed", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		int answer = JOptionPane.showConfirmDialog(this, "Are you sure want to delete this data source?", "Delete Confirmation",
				JOptionPane.OK_CANCEL_OPTION);
		if (answer == JOptionPane.OK_OPTION) {
			obdaModel.removeSource(ds.getSourceID());
		}
	}// GEN-LAST:event_cmdRemoveActionPerformed

	private void cmdTestConnectionActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cmdTestConnectionActionPerformed
		
		OBDADataSource ds = selector.getSelectedDataSource();
		if (ds == null) {
			JOptionPane.showMessageDialog(this, "Select a data source to proceed", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		lblConnectionStatus.setText("Establishing connection...");
		lblConnectionStatus.setForeground(Color.BLACK);
		
		Runnable run = new Runnable() {
			
			@Override
			public void run() {
				JDBCConnectionManager connm = JDBCConnectionManager.getJDBCConnectionManager();
				try {
						try {
							connm.closeConnection(selectedDataSource);
						} catch (Exception e) {
							
						}
					Connection conn = connm.getConnection(selectedDataSource);
					if (conn == null)
						throw new SQLException("Error connecting to the database");
					lblConnectionStatus.setForeground(Color.GREEN);
					lblConnectionStatus.setText("Connection is OK");
				} catch (SQLException e) { // if fails
					lblConnectionStatus.setForeground(Color.RED);
					lblConnectionStatus.setText(String.format("%s (ERR-CODE: %s)", e.getMessage(), e.getErrorCode()));
				} 
				
			}
		};
		SwingUtilities.invokeLater(run);
		
	}// GEN-LAST:event_cmdTestConnectionActionPerformed

	private URI createUri(String name) {
		URI uri = null;
		try {
			uri = URI.create(name);
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(null, "Invalid identifier string", "Warning", JOptionPane.WARNING_MESSAGE);
		}
		return uri;
	}

	private void fieldChangeHandler(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_fieldChangeHandler
		timer.restart();
	}// GEN-LAST:event_fieldChangeHandler

	private void updateSourceValues() {
		if (selectedDataSource == null) {
//			JOptionPane.showMessageDialog(this, "Select a data source to proceed", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		JDBCConnectionManager man = JDBCConnectionManager.getJDBCConnectionManager();
		try {
			man.closeConnection(selectedDataSource);
		} catch (OBDAException e) {
			// do nothing
		} catch (SQLException e) {
			// do nothing
		}
		String username = txtDatabaseUsername.getText();
		selectedDataSource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		String password = new String(txtDatabasePassword.getPassword());
		selectedDataSource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		String driver = txtJdbcDriver.getSelectedItem() == null ? "" : (String) txtJdbcDriver.getSelectedItem();
		selectedDataSource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		String url = txtJdbcUrl.getText();
		selectedDataSource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);

		if (url.endsWith(" ")) {
			lblConnectionStatus.setForeground(Color.RED);
			lblConnectionStatus.setText("Warning the URL ends with a white space, this can give rise to connection problems");
		} else if (driver.endsWith(" ")) {
			lblConnectionStatus.setForeground(Color.RED);
			lblConnectionStatus.setText("Warning the Driver class ends with a white space, this can give rise to connection problems");
		} else if (password.endsWith(" ")) {
			lblConnectionStatus.setForeground(Color.RED);
			lblConnectionStatus.setText("Warning the password ends with a white space, this can give rise to connection problems");
		} else if (username.endsWith(" ")) {
			lblConnectionStatus.setForeground(Color.RED);
			lblConnectionStatus.setText("Warning the password ends with a white space, this can give rise to connection problems");
		} else {
			lblConnectionStatus.setText("");
		}
		obdaModel.fireSourceParametersUpdated();
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton cmdHelp;
	private javax.swing.JButton cmdNew;
	private javax.swing.JButton cmdRemove;
	private javax.swing.JButton cmdTestConnection;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JLabel lblConnectionStatus;
	private javax.swing.JLabel lblDataSourceName;
	private javax.swing.JLabel lblDatabasePassword;
	private javax.swing.JLabel lblDatabaseUsername;
	private javax.swing.JLabel lblJdbcDriver;
	private javax.swing.JLabel lblJdbcUrl;
	private javax.swing.JLabel lblSources;
	private javax.swing.JLabel lblSourcesNumber;
	private javax.swing.JPanel pnlCommandButton;
	private javax.swing.JPanel pnlDataSourceParameters;
	private javax.swing.JPanel pnlDataSourceSelector;
	private javax.swing.JPanel pnlInformation;
	private javax.swing.JPanel pnlOBDAModelData;
	private javax.swing.JPasswordField txtDatabasePassword;
	private javax.swing.JTextField txtDatabaseUsername;
	private javax.swing.JComboBox txtJdbcDriver;
	private javax.swing.JTextField txtJdbcUrl;

	// End of variables declaration//GEN-END:variables

	private void currentDatasourceChange(OBDADataSource previousdatasource, OBDADataSource currentsource) {

		comboListener.setNotify(false);
		if (currentsource == null) {
			selectedDataSource = null;
			txtJdbcDriver.setSelectedIndex(0);
			txtDatabaseUsername.setText("");
			txtDatabasePassword.setText("");
			txtJdbcUrl.setText("");
			txtJdbcDriver.setEnabled(false);
			txtDatabaseUsername.setEnabled(false);
			txtDatabasePassword.setEnabled(false);
			txtJdbcUrl.setEnabled(false);
			lblConnectionStatus.setText("");

		} else {
			selectedDataSource = currentsource;
			txtJdbcDriver.setSelectedItem(currentsource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER));
			txtDatabaseUsername.setText(currentsource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME));
			txtDatabasePassword.setText(currentsource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD));
			txtJdbcUrl.setText(currentsource.getParameter(RDBMSourceParameterConstants.DATABASE_URL));
			txtJdbcDriver.setEnabled(true);
			txtDatabaseUsername.setEnabled(true);
			txtDatabasePassword.setEnabled(true);
			txtJdbcUrl.setEnabled(true);
			lblConnectionStatus.setText("");
		}
		comboListener.setNotify(true);
	}

	@Override
	public void datasourceChanged(OBDADataSource oldSource, OBDADataSource newSource) {
		currentDatasourceChange(oldSource, newSource);
		if (newSource == null) {
			enableFields(false);
		} else {
			enableFields(true);
		}
	}
}
