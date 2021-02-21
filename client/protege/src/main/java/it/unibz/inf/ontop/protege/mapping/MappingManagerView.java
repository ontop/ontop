package it.unibz.inf.ontop.protege.mapping;

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
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import java.awt.*;

public class MappingManagerView extends AbstractOWLViewComponent implements OBDAModelManagerListener, OWLSelectionModelListener {

	private static final long serialVersionUID = 1790921396564256165L;

	private OBDAModelManager obdaModelManager;
	private MappingManagerPanel mappingPanel;

	@Override
	protected void initialiseOWLView() {
		OWLEditorKit editorKit = getOWLEditorKit();
		obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);
		mappingPanel = new MappingManagerPanel(editorKit, obdaModelManager);

		setLayout(new BorderLayout());
		add(mappingPanel, BorderLayout.CENTER);

		obdaModelManager.addListener(this);
		getOWLWorkspace().getOWLSelectionModel().addListener(this);
	}

	@Override
	protected void disposeOWLView() {
		getOWLWorkspace().getOWLSelectionModel().removeListener(this);
		obdaModelManager.removeListener(this);
	}

	@Override
	public void activeOntologyChanged() {
		mappingPanel.setFilter("");
	}

	@Override
	public void selectionChanged() {
		OWLEntity entity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
		if (entity != null)
			mappingPanel.setFilter(getFilter(entity));
	}

	private static String getFilter(OWLEntity entity) {
		if (entity.isTopEntity())
			return "";

		IRI iri = entity.getIRI();
		if (!iri.getFragment().isEmpty())
			return iri.getFragment();

		String s = iri.toString();
		return s.substring(s.lastIndexOf("/"));
	}
}
