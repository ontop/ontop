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

import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.core.OBDAModelManagerListener;
import it.unibz.inf.ontop.protege.panels.MappingManagerPanel;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.protege.editor.owl.ui.view.Findable;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MappingsManagerView extends AbstractOWLViewComponent implements OBDAModelManagerListener, Findable<OWLEntity> {

	private static final long serialVersionUID = 1790921396564256165L;

	private OBDAModelManager obdaModelManager;

	private MappingManagerPanel mappingPanel;

	@Override
	protected void initialiseOWLView() {
		OWLEditorKit editorKit = getOWLEditorKit();

		obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);
		obdaModelManager.addListener(this);

		mappingPanel = new MappingManagerPanel(obdaModelManager);

		editorKit.getOWLWorkspace().getOWLSelectionModel().addListener(() -> {
			OWLEntity entity = editorKit.getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
			if (entity == null)
				return;

			if (entity.isTopEntity()) {
				mappingPanel.setFilter("");
			}
			else {
				IRI iri = entity.getIRI();
				if (iri.getFragment().isEmpty()) {
					String s = iri.toString();
					mappingPanel.setFilter(s.substring(s.lastIndexOf("/")));
				}
				else {
					mappingPanel.setFilter(iri.getFragment());
				}
			}
		});
		mappingPanel.datasourceChanged();

		setLayout(new BorderLayout());
        add(mappingPanel, BorderLayout.CENTER);
	}

	@Override
	protected void disposeOWLView() {
		obdaModelManager.removeListener(this);
	}

	@Override
	public void activeOntologyChanged() {
		SwingUtilities.invokeLater(() -> mappingPanel.datasourceChanged());
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
