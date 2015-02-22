package org.semanticweb.ontop.protege4.gui.preferences;

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

import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.protege4.core.ProtegeReformulationPlatformPreferences;
import org.semanticweb.ontop.protege4.panels.ConfigPanel;

public class OBDAOWLReformulationPlatformConfigPanel extends OWLPreferencesPanel {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 2017399622537704497L;
	private ProtegeReformulationPlatformPreferences preference = null;
	private ConfigPanel configPanel = null;
	
	@Override
	public void applyChanges() {
		// Do nothing.
	}

	@Override
	public void initialise() throws Exception {
		preference = (ProtegeReformulationPlatformPreferences)
			getEditorKit().get(QuestPreferences.class.getName());
		
		this.setLayout(new BorderLayout());
		configPanel = new ConfigPanel(preference);
		this.add(configPanel,BorderLayout.CENTER);
	}

	@Override
	public void dispose() throws Exception {
		// Do nothing.
	}
}
