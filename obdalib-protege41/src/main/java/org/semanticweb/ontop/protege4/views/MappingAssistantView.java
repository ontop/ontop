package org.semanticweb.ontop.protege4.views;

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.model.impl.OBDAModelImpl;
import org.semanticweb.ontop.protege4.core.OBDAModelWrapper;
import org.semanticweb.ontop.protege4.core.OBDAModelManager;
import org.semanticweb.ontop.protege4.core.OBDAModelManagerListener;
import org.semanticweb.ontop.protege4.panels.DatasourceSelector;
import org.semanticweb.ontop.protege4.panels.MappingAssistantPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingAssistantView extends AbstractOWLViewComponent implements OBDAModelManagerListener {

	private static final long serialVersionUID = 1L;

	private DatasourceSelector datasourceSelector;

	private OBDAModelManager apic;

	private static final Logger log = LoggerFactory.getLogger(SQLQueryInterfaceView.class);
	
	@Override
	protected void disposeOWLView() {
		apic.removeListener(this);
	}

	@Override
	protected void initialiseOWLView() throws Exception {

		apic = (OBDAModelManager) getOWLEditorKit().get(OBDAModelImpl.class.getName());
		apic.addListener(this);

		OBDAModelWrapper dsController = apic.getActiveOBDAModelWrapper();


		MappingAssistantPanel queryPanel = new MappingAssistantPanel(dsController, apic.getNativeQLFactory());
		datasourceSelector = new DatasourceSelector(dsController);
		datasourceSelector.addDatasourceListListener(queryPanel);

		JPanel selectorPanel = new JPanel();
		selectorPanel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Select datasource: ");

		label.setFont(new Font("Dialog", Font.BOLD, 12));
		label.setForeground(new Color(53, 113, 163));

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

		selectorPanel.setBorder(new TitledBorder("Datasource selection"));
		queryPanel.setBorder(new TitledBorder("SQL Query Editor"));

		setLayout(new BorderLayout());
		add(queryPanel, BorderLayout.CENTER);
		add(selectorPanel, BorderLayout.NORTH);

		log.debug("SQL Query view initialized");
	}

	@Override
	public void activeOntologyChanged() {
		datasourceSelector.setDatasourceController(apic.getActiveOBDAModelWrapper());
	}
}
