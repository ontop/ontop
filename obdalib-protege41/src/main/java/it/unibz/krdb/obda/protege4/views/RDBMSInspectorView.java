/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.views;

import it.unibz.krdb.obda.gui.swing.panel.DatasourceSelector;
import it.unibz.krdb.obda.gui.swing.panel.SQLSchemaInspectorPanel;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.protege4.core.OBDAModelManagerListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class RDBMSInspectorView extends AbstractOWLViewComponent implements OBDAModelManagerListener {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -1168890804596237449L;
	private static final Logger	log					= Logger.getLogger(RDBMSInspectorView.class);

	OBDAModelManager			apic				= null;

	SQLSchemaInspectorPanel		inspectorPanel		= null;

	DatasourceSelector			datasourceSelector	= null;

	@Override
	protected void disposeOWLView() {
		apic.removeListener(this);
	}

	@Override
	protected void initialiseOWLView() throws Exception {

		apic = (OBDAModelManager)getOWLEditorKit().get(OBDAModelImpl.class.getName());
		apic.addListener(this);

		OBDAModel dsController = apic.getActiveOBDAModel();

		inspectorPanel = new SQLSchemaInspectorPanel();
		datasourceSelector = new DatasourceSelector(dsController);
		
		datasourceSelector.addDatasourceListListener(inspectorPanel);
		
		

		/* GUI */
		setBackground(new java.awt.Color(153, 0, 0));

		JPanel selectorPanel = new JPanel();
		selectorPanel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Select datasource: ");
		label.setBackground(new java.awt.Color(153, 153, 153));
		label.setFont(new java.awt.Font("Arial", 1, 11));
		label.setForeground(new java.awt.Color(153, 153, 153));
		label.setPreferredSize(new Dimension(119, 14));

		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		selectorPanel.add(label, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		selectorPanel.add(datasourceSelector, gridBagConstraints);

		selectorPanel.setBorder(new TitledBorder("Datasource Selection"));
		inspectorPanel.setBorder(new TitledBorder("SQL Schema Inspector"));

		setLayout(new BorderLayout());
		add(inspectorPanel, BorderLayout.CENTER);
		add(selectorPanel, BorderLayout.NORTH);

	}

	@Override
	public void activeOntologyChanged() {
		datasourceSelector.setDatasourceController(apic.getActiveOBDAModel());
	}
}
