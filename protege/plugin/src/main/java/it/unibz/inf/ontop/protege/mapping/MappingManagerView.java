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
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import java.awt.*;

public class MappingManagerView extends AbstractOWLViewComponent implements OWLSelectionModelListener {

	private static final long serialVersionUID = 1790921396564256165L;

	private OBDAModelManager obdaModelManager;
	private MappingManagerPanel panel;

	@Override
	protected void initialiseOWLView() {
		OWLEditorKit editorKit = getOWLEditorKit();
		obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);

		panel = new MappingManagerPanel(editorKit);
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);

		obdaModelManager.addListener(panel);
		getOWLWorkspace().getOWLSelectionModel().addListener(this);
		obdaModelManager.addMappingListener(panel.getTriplesMapManagerListener());
	}

	@Override
	protected void disposeOWLView() {
		obdaModelManager.removeMappingListener(panel.getTriplesMapManagerListener());
		getOWLWorkspace().getOWLSelectionModel().removeListener(this);
		obdaModelManager.removeListener(panel);
	}


	@Override
	public void selectionChanged() {
		OWLEntity entity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
		if (entity != null)
			panel.setFilter(getFilter(entity));
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
