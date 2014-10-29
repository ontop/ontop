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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.protege.editor.owl.ui.view.Findable;
import org.semanticweb.ontop.io.TargetQueryVocabularyValidator;
import org.semanticweb.ontop.model.impl.OBDAModelImpl;
import org.semanticweb.ontop.owlapi3.TargetQueryValidator;
import org.semanticweb.ontop.protege4.core.OBDAModelFacade;
import org.semanticweb.ontop.protege4.core.OBDAModelManager;
import org.semanticweb.ontop.protege4.core.OBDAModelManagerListener;
import org.semanticweb.ontop.protege4.panels.DatasourceSelector;
import org.semanticweb.ontop.protege4.panels.MappingManagerPanel;
import org.semanticweb.owlapi.model.OWLEntity;

public class MappingsManagerView extends AbstractOWLViewComponent implements OBDAModelManagerListener, Findable<OWLEntity> {

	private static final long serialVersionUID = 1790921396564256165L;

	OBDAModelManager controller = null;

	DatasourceSelector datasourceSelector = null;

	MappingManagerPanel mappingPanel = null;

	@Override
	protected void disposeOWLView() {
		controller.removeListener(this);
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		
		// Retrieve the editor kit.
		final OWLEditorKit editor = getOWLEditorKit();

		controller = (OBDAModelManager) editor.get(OBDAModelImpl.class.getName());
		controller.addListener(this);

		OBDAModelFacade obdaModel = controller.getActiveOBDAModelFacade();
		
		TargetQueryVocabularyValidator validator = new TargetQueryValidator(obdaModel.getCurrentImmutableOBDAModel());
		
		// Init the Mapping Manager panel.
		mappingPanel = new MappingManagerPanel(obdaModel, validator);

		editor.getOWLWorkspace().getOWLSelectionModel().addListener(new OWLSelectionModelListener() {
			@Override
			public void selectionChanged() throws Exception {
				OWLEntity entity = editor.getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
				if (entity == null)
					return;
				if (!entity.isTopEntity()) {
					String shortf = entity.getIRI().getFragment();
					if (shortf == null) {
						String iri = entity.getIRI().toString();
						shortf = iri.substring(iri.lastIndexOf("/"));
					}
					mappingPanel.setFilter("pred:" + shortf);
				} else {
					mappingPanel.setFilter("");
				}
			}
		});

		datasourceSelector = new DatasourceSelector(controller.getActiveOBDAModelFacade());
		datasourceSelector.addDatasourceListListener(mappingPanel);

		// Construt the layout of the panel.
		JPanel selectorPanel = new JPanel();
		selectorPanel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Select datasource: ");
		label.setFont(new Font("Dialog", Font.BOLD, 12));
		label.setForeground(new Color(53,113,163));
		// label.setBackground(new java.awt.Color(153, 153, 153));
		// label.setFont(new java.awt.Font("Arial", 1, 11));
		// label.setForeground(new java.awt.Color(153, 153, 153));
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

		selectorPanel.setBorder(new TitledBorder("Datasource selection"));
		mappingPanel.setBorder(new TitledBorder("Mapping manager"));

		setLayout(new BorderLayout());
		add(mappingPanel, BorderLayout.CENTER);
		add(selectorPanel, BorderLayout.NORTH);
	}

	@Override
	public void activeOntologyChanged() {
		OBDAModelFacade obdaModel = controller.getActiveOBDAModelFacade();
		TargetQueryVocabularyValidator validator = new TargetQueryValidator(obdaModel.getCurrentImmutableOBDAModel());

		mappingPanel.setOBDAModel(obdaModel);
		mappingPanel.setTargetQueryValidator(validator);
		datasourceSelector.setDatasourceController(obdaModel);
	}

	@Override
	public List<OWLEntity> find(String match) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void show(OWLEntity owlEntity) {
	//	System.out.println(owlEntity);
	}
}
