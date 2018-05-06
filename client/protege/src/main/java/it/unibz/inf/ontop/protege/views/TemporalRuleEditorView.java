package it.unibz.inf.ontop.protege.views;

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

import it.unibz.inf.ontop.protege.core.*;
import it.unibz.inf.ontop.protege.panels.TemporalRuleEditorPanel;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.border.TitledBorder;
import java.awt.*;

public class TemporalRuleEditorView extends AbstractOWLViewComponent implements OBDAModelManagerListener {
	private TemporalOBDAModelManager controller = null;
	private TemporalRuleEditorPanel ruleEditorPanel = null;

	@Override
	protected void disposeOWLView() {
		controller.removeListener(this);
	}

	@Override
	protected void initialiseOWLView() {
		
		// Retrieve the editor kit.
		final OWLEditorKit editor = getOWLEditorKit();

		controller = (TemporalOBDAModelManager) editor.get(TemporalOBDAModelManager.class.getName());
		controller.addListener(this);
		
		// Init the Mapping Manager panel.
		ruleEditorPanel = new TemporalRuleEditorPanel(controller.getActiveOBDAModel());

		editor.getOWLWorkspace().getOWLSelectionModel().addListener(() -> {
			OWLEntity entity = editor.getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
			if (entity == null)
				return;
			if (!entity.isTopEntity()) {
				String shortf = entity.getIRI().getFragment();
				if (shortf == null) {
					String iri = entity.getIRI().toString();
					shortf = iri.substring(iri.lastIndexOf("/"));
				}
				ruleEditorPanel.setFilter("pred:" + shortf);
			} else {
				ruleEditorPanel.setFilter("");
			}
		});

		ruleEditorPanel.setBorder(new TitledBorder("Temporal Rule Editor"));

		setLayout(new BorderLayout());
        add(ruleEditorPanel, BorderLayout.CENTER);

	}

	@Override
	public void activeOntologyChanged() {
		ruleEditorPanel.ontologyChanged(controller.getActiveOBDAModel());
	}
}
