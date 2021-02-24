package it.unibz.inf.ontop.protege.query;

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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unibz.inf.ontop.protege.core.OBDAModelManagerListener;
import org.protege.editor.core.Disposable;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

import javax.annotation.Nonnull;

public class QueryManagerView extends AbstractOWLViewComponent {

	private static final long serialVersionUID = 1L;
	
	private QueryManagerPanel panel;
	private OBDAModelManager obdaModelManager;

	@Override
	protected void initialiseOWLView()  {
		OWLEditorKit editorKit = getOWLEditorKit();
		obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);

		setLayout(new BorderLayout());
		panel = new QueryManagerPanel(editorKit);
		add(panel, BorderLayout.CENTER);

		obdaModelManager.addQueryManagerListener(panel.getQueryManagerListener());
		obdaModelManager.addListener(panel.getOBDAModelManagerListener());

		List<QueryManagerView> queryManagerViews = getList(editorKit);
		if (queryManagerViews.isEmpty())
			queryManagerViews = new QueryManagerViewsList(editorKit);

		queryManagerViews.add(this);

		for (QueryInterfaceView queryInterfaceView : QueryInterfaceView.getList(editorKit))
			addSelectionListener(queryInterfaceView.getSelectionListener());
	}

	@Override
	protected void disposeOWLView() {
		List<QueryManagerView> queryManagerViews = getList(getOWLEditorKit());
		queryManagerViews.remove(this);

		obdaModelManager.removeQueryManagerListener(panel.getQueryManagerListener());
		obdaModelManager.removeListener(panel.getOBDAModelManagerListener());

	}

	public void addSelectionListener(QueryManagerPanelSelectionListener listener) {
		panel.addQueryManagerSelectionListener(listener);
	}

	public void removeSelectionListener(QueryManagerPanelSelectionListener listener) {
		panel.removeQueryManagerSelectionListener(listener);
	}

	@Nonnull
	public static List<QueryManagerView> getList(OWLEditorKit editorKit) {
		QueryManagerViewsList list = (QueryManagerViewsList) editorKit.get(QueryManagerViewsList.class.getName());
		return list == null ? Collections.emptyList() : list;
	}

	private static class QueryManagerViewsList extends ArrayList<QueryManagerView> implements Disposable {

		private static final long serialVersionUID = 2986737849606126197L;

		private QueryManagerViewsList(OWLEditorKit editorKit) {
			editorKit.put(QueryManagerViewsList.class.getName(), this);
		}

		@Override
		public void dispose() { /* NO-OP */ }
	}
}
