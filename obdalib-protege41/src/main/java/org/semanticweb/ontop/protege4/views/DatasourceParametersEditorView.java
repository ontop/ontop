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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.ontop.model.impl.SQLOBDAModelImpl;
import org.semanticweb.ontop.protege4.core.OBDAModelManager;
import org.semanticweb.ontop.protege4.core.OBDAModelManagerListener;
import org.semanticweb.ontop.protege4.panels.DatasourceParameterEditorPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasourceParametersEditorView extends AbstractOWLViewComponent implements OBDAModelManagerListener {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(DatasourceParametersEditorView.class);

	private DatasourceParameterEditorPanel panel;

	private OBDAModelManager apic;

	@Override
	protected void disposeOWLView() {
		apic.removeListener(this);
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		
		
		
		
		apic = (OBDAModelManager) getOWLEditorKit().get(SQLOBDAModelImpl.class.getName());
		apic.addListener(this);

		panel = new DatasourceParameterEditorPanel(apic.getActiveOBDAModel());
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		
		setLayout(new GridBagLayout());
		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        add(panel, gridBagConstraints);
        
//		add(panel, BorderLayout.CENTER);
		
		log.debug("Datasource parameter view Component initialized");
	}

	@Override
	public void activeOntologyChanged() {
		panel.setDatasourcesController(apic.getActiveOBDAModel());
	}
}
