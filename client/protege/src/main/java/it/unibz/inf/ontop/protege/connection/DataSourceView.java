package it.unibz.inf.ontop.protege.connection;

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
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class DataSourceView extends AbstractOWLViewComponent {

	private static final long serialVersionUID = -4515710047558710080L;

	private OBDAModelManager obdaModelManager;
	private DataSourcePanel panel;

	@Override
	protected void initialiseOWLView()  {
		OWLEditorKit editorKit = getOWLEditorKit();
		obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);

		panel = new DataSourcePanel(editorKit);
		setLayout(new BorderLayout());
		add(panel, BorderLayout.NORTH);

		obdaModelManager.addListener(panel);
	}

	@Override
	protected void disposeOWLView() {
		obdaModelManager.removeListener(panel);
	}
}
