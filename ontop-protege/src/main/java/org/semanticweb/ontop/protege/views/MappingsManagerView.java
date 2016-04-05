package org.semanticweb.ontop.protege.views;

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

import it.unibz.krdb.obda.io.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.owlapi3.TargetQueryValidator;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.protege.editor.owl.ui.view.Findable;
import org.semanticweb.ontop.protege.core.OBDAModelManager;
import org.semanticweb.ontop.protege.core.OBDAModelManagerListener;
import org.semanticweb.ontop.protege.panels.MappingManagerPanel;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class MappingsManagerView extends AbstractOWLViewComponent implements OBDAModelManagerListener, Findable<OWLEntity> {

	private static final long serialVersionUID = 1790921396564256165L;

	OBDAModelManager controller = null;

	OBDAModel obdaModel;

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

		obdaModel = controller.getActiveOBDAModel();
		
		TargetQueryVocabularyValidator validator = new TargetQueryValidator(obdaModel.getOntologyVocabulary());
		
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
		if (obdaModel.getSources().size() > 0) {
			mappingPanel.datasourceChanged(mappingPanel.getSelectedSource(), obdaModel.getSources().get(0));
		}
		
		mappingPanel.setBorder(new TitledBorder("Mapping manager"));

		setLayout(new BorderLayout());
		//add(mappingPanel, BorderLayout.NORTH);
        add(mappingPanel, BorderLayout.CENTER);
		
	}

	@Override
	public void activeOntologyChanged() {
		obdaModel = controller.getActiveOBDAModel();
		TargetQueryVocabularyValidator validator = new TargetQueryValidator(obdaModel.getOntologyVocabulary());

		mappingPanel.setTargetQueryValidator(validator);

		mappingPanel.datasourceChanged(mappingPanel.getSelectedSource(), obdaModel.getSources().get(0));
		
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
