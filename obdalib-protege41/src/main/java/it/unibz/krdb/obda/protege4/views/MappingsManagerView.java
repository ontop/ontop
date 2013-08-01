/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.views;

import it.unibz.krdb.obda.io.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.owlapi3.TargetQueryValidator;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.protege4.core.OBDAModelManagerListener;
import it.unibz.krdb.obda.protege4.panels.DatasourceSelector;
import it.unibz.krdb.obda.protege4.panels.MappingManagerPanel;

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

		OBDAModel obdaModel = controller.getActiveOBDAModel();
		
		TargetQueryVocabularyValidator validator = new TargetQueryValidator(obdaModel);
		
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

		datasourceSelector = new DatasourceSelector(controller.getActiveOBDAModel());
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
		OBDAModel obdaModel = controller.getActiveOBDAModel();
		TargetQueryVocabularyValidator validator = new TargetQueryValidator(obdaModel);

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
