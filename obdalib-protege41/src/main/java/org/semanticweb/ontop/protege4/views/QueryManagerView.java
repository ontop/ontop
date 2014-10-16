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

import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.ontop.model.impl.OBDAModelImpl;
import org.semanticweb.ontop.protege4.core.OBDAModelManager;
import org.semanticweb.ontop.protege4.core.OBDAModelManagerListener;
import org.semanticweb.ontop.protege4.panels.SavedQueriesPanel;
import org.semanticweb.ontop.protege4.panels.SavedQueriesPanelListener;

public class QueryManagerView extends AbstractOWLViewComponent implements OBDAModelManagerListener {

	private static final long serialVersionUID = 1L;
	
	private SavedQueriesPanel panel;
	
	private OBDAModelManager obdaController;

	@Override
	protected void disposeOWLView() {
		QueryManagerViewsList queryManagerViews = (QueryManagerViewsList) this.getOWLEditorKit().get(QueryManagerViewsList.class.getName());
		if (queryManagerViews == null) {
			return;
		}
		queryManagerViews.remove(this);
		obdaController.removeListener(this);
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		obdaController = (OBDAModelManager) getOWLEditorKit().get(OBDAModelImpl.class.getName());
		obdaController.addListener(this);

		setLayout(new BorderLayout());
		panel = new SavedQueriesPanel(obdaController.getQueryController());

		add(panel, BorderLayout.CENTER);

		registerInEditorKit();
	}

	public void addListener(SavedQueriesPanelListener listener) {
		panel.addQueryManagerListener(listener);
	}

	public void removeListener(SavedQueriesPanelListener listener) {
		panel.removeQueryManagerListener(listener);
	}

	public void registerInEditorKit() {
		QueryManagerViewsList queryManagerViews = (QueryManagerViewsList) this.getOWLEditorKit().get(QueryManagerViewsList.class.getName());
		if (queryManagerViews == null) {
			queryManagerViews = new QueryManagerViewsList();
			getOWLEditorKit().put(QueryManagerViewsList.class.getName(), queryManagerViews);
		}
		QueryInterfaceViewsList queryInterfaceViews = (QueryInterfaceViewsList) this.getOWLEditorKit().get(QueryInterfaceViewsList.class.getName());
		if ((queryInterfaceViews != null) && (!queryInterfaceViews.isEmpty())) {
			for (QueryInterfaceView queryInterfaceView : queryInterfaceViews) {
				this.addListener(queryInterfaceView);
			}
		}
		queryManagerViews.add(this);
	}

	@Override
	public void activeOntologyChanged() {
		// NO-OP
	}
}
