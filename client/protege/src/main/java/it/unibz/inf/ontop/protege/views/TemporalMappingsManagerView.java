package it.unibz.inf.ontop.protege.views;

/*
 * #%L
 * ontop-protege
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

import it.unibz.inf.ontop.protege.core.OBDAModelManagerListener;
import it.unibz.inf.ontop.protege.core.TemporalOBDAModel;
import it.unibz.inf.ontop.protege.core.TemporalOBDAModelManager;
import it.unibz.inf.ontop.protege.panels.TemporalMappingManagerPanel;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.border.TitledBorder;
import java.awt.*;

public class TemporalMappingsManagerView extends AbstractOWLViewComponent implements OBDAModelManagerListener {
	private TemporalOBDAModelManager controller = null;
	private TemporalMappingManagerPanel mappingPanel = null;

	@Override
	protected void disposeOWLView() {
		controller.removeListener(this);
	}

	@Override
	protected void initialiseOWLView() {
		controller = (TemporalOBDAModelManager) getOWLEditorKit().get(TemporalOBDAModelManager.class.getName());
		controller.addListener(this);

		TemporalOBDAModel obdaModel = controller.getActiveOBDAModel();

		mappingPanel = new TemporalMappingManagerPanel(obdaModel);

		getOWLEditorKit().getOWLWorkspace().getOWLSelectionModel().addListener(() -> {
			OWLEntity entity = getOWLEditorKit().getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
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
		});
		mappingPanel.setBorder(new TitledBorder("Temporal Mapping Manager"));
		if (obdaModel.getSources().size() > 0) {
			mappingPanel.updateModel(obdaModel);
		}
		setLayout(new BorderLayout());
        add(mappingPanel, BorderLayout.CENTER);

	}

	@Override
	public void activeOntologyChanged() {
		mappingPanel.updateModel(controller.getActiveOBDAModel());
	}
}
