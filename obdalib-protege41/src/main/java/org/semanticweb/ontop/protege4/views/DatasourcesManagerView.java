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
import org.semanticweb.ontop.model.impl.SQLOBDAModelImpl;
import org.semanticweb.ontop.protege4.core.OBDAModelManager;
import org.semanticweb.ontop.protege4.core.OBDAModelManagerListener;
import org.semanticweb.ontop.protege4.panels.DatasourceParameterEditorPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasourcesManagerView extends AbstractOWLViewComponent implements OBDAModelManagerListener {

	private static final long serialVersionUID = -4515710047558710080L;

	private static final Logger log = LoggerFactory.getLogger(DatasourcesManagerView.class);

	DatasourceParameterEditorPanel editor;

	OBDAModelManager apic = null;

	@Override
	protected void disposeOWLView() {
		apic.removeListener(this);
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		
		apic = (OBDAModelManager) getOWLEditorKit().get(SQLOBDAModelImpl.class.getName());
		apic.addListener(this);

		setLayout(new BorderLayout());

		editor = new DatasourceParameterEditorPanel(apic.getActiveOBDAModel());
		add(editor, BorderLayout.NORTH);

		log.debug("Datasource browser initialized");
	}

	@Override
	public void activeOntologyChanged() {
		editor.setDatasourcesController(apic.getActiveOBDAModel());
	}
}
