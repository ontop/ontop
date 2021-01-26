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

import it.unibz.inf.ontop.protege.core.OBDADataSource;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.panels.DatasourceParameterEditorPanel;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class DatasourcesManagerView extends AbstractOWLViewComponent {

	private static final long serialVersionUID = -4515710047558710080L;

	private static final Logger log = LoggerFactory.getLogger(DatasourcesManagerView.class);

	private DatasourceParameterEditorPanel editor;

	private OBDADataSource datasource;

	@Override
	protected void initialiseOWLView()  {
		OBDAModelManager obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(getOWLEditorKit());
		datasource = obdaModelManager.getDatasource();

		editor = new DatasourceParameterEditorPanel(datasource);
		setLayout(new BorderLayout());
		add(editor, BorderLayout.NORTH);

		datasource.addListener(editor);
		datasource.fireChanged();

		log.debug("Datasource browser initialized");
	}

	@Override
	protected void disposeOWLView() {
		datasource.removeListener(editor);
	}
}
